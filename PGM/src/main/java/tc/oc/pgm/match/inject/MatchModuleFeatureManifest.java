package tc.oc.pgm.match.inject;

import java.util.List;
import javax.annotation.Nullable;
import javax.inject.Provider;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import tc.oc.commons.core.inject.Keys;
import tc.oc.commons.core.reflect.ResolvableType;
import tc.oc.pgm.features.FeatureDefinition;
import tc.oc.pgm.match.MatchModule;

/**
 * Configure a match module {@link M} that loads if and only if at least one instance of feature {@link F} is defined.
 *
 * A binding for List<F> must exist.
 */
public class MatchModuleFeatureManifest<M extends MatchModule, F extends FeatureDefinition> extends MatchModuleFixtureManifest<M> {

    private final TypeLiteral<F> featureType;

    protected MatchModuleFeatureManifest() {
        this(null, null);
    }

    public MatchModuleFeatureManifest(@Nullable TypeLiteral<M> moduleType, @Nullable TypeLiteral<F> featureType) {
        super(moduleType);
        this.featureType = featureType != null ? featureType : new ResolvableType<F>(){}.in(getClass());
    }

    @Override
    protected void configure() {
        super.configure();
        featureListProvider = getProvider(Keys.listOf(Key.get(featureType)));
    }

    private Provider<List<F>> featureListProvider;

    @Override
    protected boolean shouldLoad() {
        return !featureListProvider.get().isEmpty();
    }
}
