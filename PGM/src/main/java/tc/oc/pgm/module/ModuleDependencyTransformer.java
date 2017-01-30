package tc.oc.pgm.module;

import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.HasDependencies;
import tc.oc.commons.core.inject.Keys;
import tc.oc.commons.core.inject.Transformer;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.commons.core.util.Streams;

public class ModuleDependencyTransformer<M> implements Transformer<Optional<M>>, HasDependencies {

    private final Set<Key<?>> requiresKeys;
    private final Set<Key<Optional<?>>> dependsKeys, followsKeys;
    private final Set<Dependency<?>> dependencies;

    public ModuleDependencyTransformer(TypeLiteral<M> type) {
        final ModuleDescription annotation = type.getRawType().getAnnotation(ModuleDescription.class);
        if(annotation == null) {
            requiresKeys = ImmutableSet.of();
            dependsKeys = followsKeys = ImmutableSet.of();
        } else {
            requiresKeys = Keys.get(annotation.requires());
            dependsKeys = Keys.optional(annotation.depends());
            followsKeys = Keys.optional(annotation.follows());
        }

        dependencies = Streams.concat(requiresKeys.stream(),
                                      dependsKeys.stream(),
                                      followsKeys.stream())
                              .map(Dependency::get)
                              .collect(Collectors.toImmutableSet());
    }

    private Set<Provider<?>> requiresProviders;
    private Set<Provider<Optional<?>>> dependsProviders;
    private Set<Provider<Optional<?>>> followsProviders;

    @Inject void buildProviders(Injector injector) {
        requiresProviders = getProviders(injector, (Set) requiresKeys);
        dependsProviders = getProviders(injector, dependsKeys);
        followsProviders = getProviders(injector, followsKeys);
    }

    private <D> Set<Provider<D>> getProviders(Injector injector, Set<Key<D>> keys) {
        return keys.stream()
                   .map(injector::getProvider)
                   .collect(Collectors.toImmutableSet());
    }

    @Override
    public Set<Dependency<?>> getDependencies() {
        return dependencies;
    }

    @Override
    public Optional<M> transform(Provider<Optional<M>> provider) {
        // Provision these or die trying
        requiresProviders.forEach(Provider::get);

        // If any of these are empty, so are we
        if(!dependsProviders.stream().allMatch(p -> p.get().isPresent())) {
            return Optional.empty();
        }

        // Try to provision these, but we don't care if they are empty
        followsProviders.forEach(Provider::get);

        return provider.get();
    }
}
