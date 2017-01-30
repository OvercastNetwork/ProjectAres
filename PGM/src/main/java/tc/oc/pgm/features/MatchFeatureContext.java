package tc.oc.pgm.features;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.inject.Inject;

import com.google.common.util.concurrent.UncheckedExecutionException;
import com.google.inject.ImplementedBy;
import tc.oc.commons.core.util.CachingTypeMap;
import tc.oc.commons.core.util.Optionals;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.module.ModuleLoadException;

import static tc.oc.commons.core.util.Utils.ifInstance;

@ImplementedBy(MatchFeatureContextImpl.class)
public interface MatchFeatureContext {

    <T extends Feature<?>> Optional<T> bySlug(Class<T> type, String slug);

    Optional<Feature<?>> bySlug(String slug);

    <T extends Feature<?>> T get(FeatureFactory<T> factory);

    Stream<? extends Feature<?>> all();

    <T extends Feature<?>> Stream<? extends T> all(Class<T> type);
}

class MatchFeatureContextImpl implements MatchFeatureContext {

    private final Match match;
    private final Map<String, SluggedFeature<?>> bySlug = new HashMap<>();
    private final Map<FeatureFactory<?>, Feature<?>> byFactory = new HashMap<>();
    private final CachingTypeMap<Feature<?>, Feature<?>> byType = CachingTypeMap.create();
    private final Set<FeatureFactory<?>> creating = new HashSet<>();

    @Inject MatchFeatureContextImpl(Match match) {
        this.match = match;
    }

    @Override
    public <T extends Feature<?>> Optional<T> bySlug(Class<T> type, String slug) {
        return Optionals.cast(bySlug(slug), type);
    }

    @Override
    public Optional<Feature<?>> bySlug(String slug) {
        return Optional.ofNullable(bySlug.get(slug));
    }

    @Override
    public <T extends Feature<?>> T get(FeatureFactory<T> factory) {
        // Do NOT use computeIfAbsent for this!
        // The factory may try to get other features while it's creating this one,
        // and computeIfAbsent handles reentrancy VERY badly,
        // even if the keys are different.

        T feature = (T) byFactory.get(factory);
        if(feature != null) return feature;

        if(!creating.add(factory)) {
            throw new IllegalStateException("Recursive creation of feature for " + factory);
        }

        try {
            feature = factory.createFeature(match);
        } catch(ModuleLoadException e) {
            // TODO: can we do better than this?
            throw new UncheckedExecutionException(e);
        } finally {
            creating.remove(factory);
        }

        byFactory.put(factory, feature);

        byType.put((Class<? extends Feature<?>>) feature.getClass(), feature);
        byType.invalidate();

        ifInstance(feature, SluggedFeature.class, slugged ->
            bySlug.put(slugged.slug(), slugged)
        );

        match.registerEventsAndRepeatables(feature);

        return feature;
    }

    @Override
    public Stream<? extends Feature<?>> all() {
        return byFactory.values().stream();
    }

    @Override
    public <T extends Feature<?>> Stream<? extends T> all(Class<T> type) {
        return (Stream<? extends T>) byType.allAssignableTo(type).stream();
    }
}
