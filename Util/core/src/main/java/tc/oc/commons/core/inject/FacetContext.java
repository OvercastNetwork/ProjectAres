package tc.oc.commons.core.inject;

import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.reflect.TypeToken;
import tc.oc.commons.core.concurrent.ExceptionHandlingExecutor;
import tc.oc.commons.core.exception.ExceptionHandler;
import tc.oc.commons.core.util.AmbiguousElementException;
import tc.oc.commons.core.util.CachingTypeMap;

import static tc.oc.commons.core.IterableUtils.reverseForEach;
import static tc.oc.commons.core.exception.LambdaExceptionUtils.rethrowConsumer;

/**
 * A collection of {@link Facet}s, keyed by type, and ordered by dependency relationships.
 * Lifecycle callbacks can also be dispatched through this collection.
 *
 * Facets are bound into the collection using an appropriately typed {@link FacetBinder}.
 *
 * Dependencies are detected automatically from {@link Inject}ion points.
 */
public abstract class FacetContext<F extends Facet> {

    private final Class<F> baseType = (Class<F>) new TypeToken<F>(getClass()){}.getRawType();
    private final Set<F> ordered = new LinkedHashSet<>();
    private final CachingTypeMap<F, F> byType = CachingTypeMap.create();
    private boolean initialized = false;

    /**
     * Facets often depend on the thing they are facets of, and that thing usually
     * depends on the facet context, so to avoid the circular dependency, we inject
     * the set as a Provider, and don't touch it until after injection, when the
     * context is enabled.
     */
    @Inject private Provider<Set<F>> facets;

    private ExceptionHandlingExecutor exceptionHandlingExecutor;

    @Inject private void inject(ExceptionHandler exceptionHandler) {
        this.exceptionHandlingExecutor = () -> exceptionHandler;
    }

    private void discoverWithDependencies(Class<? extends F> type, Stack<F> stack) {
        final F facet = facet(type);
        if(!ordered.contains(facet)) {
            final int index = stack.indexOf(facet);
            if(index != -1) {
                // Guice supports this with proxies, but we don't, for now
                throw new IllegalStateException(
                    "Circular facet dependency: " +
                    Stream.concat(stack.subList(index, stack.size()).stream(), Stream.of(facet))
                          .map(f -> f.getClass().getSimpleName())
                          .collect(Collectors.joining(" -> "))
                );
            }

            stack.push(facet);
            try {
                Injection.dependencies(type).forEach(dependency -> {
                    final Class<?> depType = Injection.dependencyType(dependency.getKey().getTypeLiteral()).getRawType();
                    if(baseType.isAssignableFrom(depType)) {
                        discoverWithDependencies(depType.asSubclass(baseType), stack);
                    }
                });
            } finally {
                stack.pop();
            }

            ordered.add(facet);
            discover(facet);
        }
    }

    private void init() {
        if(!initialized) {
            initialized = true;

            // First, dump all facets into a TypeMap so we can find them efficiently
            for(F facet : facets.get()) {
                byType.put((Class<? extends F>) facet.getClass(), facet);
            }

            // Then, discover all their dependencies and build a partial ordering.
            // We don't inject the dependencies, Guice has already done that,
            // we just need to know what order the facets should be enabled in.
            for(F facet : facets.get()) {
                discoverWithDependencies((Class<? extends F>) facet.getClass(), new Stack<>());
            }
        }
    }

    public Set<F> all() {
        init();
        return ordered;
    }

    public @Nullable <T extends F> T getOrNull(Class<T> type) {
        init();
        try {
            return (T) byType.oneAssignableTo(type);
        } catch(NoSuchElementException e) {
            return null;
        } catch(AmbiguousElementException e) {
            throw new IllegalStateException("Multiple instances of facet " + type.getName() + " are loaded");
        }
    }

    public <T extends F> Optional<T> facetMaybe(Class<T> type) {
        return Optional.ofNullable(getOrNull(type));
    }

    public <T extends F> T facet(Class<T> type) {
        final T facet = getOrNull(type);

        if(facet == null) {
            throw new IllegalStateException("Facet " + type.getName() + " is not loaded");
        }

        return facet;
    }

    public void enableAll() {
        exceptionHandlingExecutor.executeCatch(this::enableAllThrows);
    }

    public void disableAll() {
        exceptionHandlingExecutor.executeCatch(this::disableAllThrows);
    }

    public void enableAllThrows() throws Exception {
        all().forEach(rethrowConsumer(this::enableFacet));
    }

    public void disableAllThrows() throws Exception {
        reverseForEach(all(), rethrowConsumer(this::disableFacet));
    }

    protected void enableFacet(F facet) throws Exception {
        facet.enable();
    }

    protected void disableFacet(F facet) throws Exception {
        facet.disable();
    }

    protected void discover(F facet) {}
}
