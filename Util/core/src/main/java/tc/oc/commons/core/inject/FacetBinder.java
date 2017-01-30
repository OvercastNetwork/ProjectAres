package tc.oc.commons.core.inject;

import java.lang.annotation.Annotation;
import java.util.Optional;
import javax.annotation.Nullable;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import tc.oc.commons.core.reflect.ResolvableType;
import tc.oc.commons.core.util.Optionals;

public class FacetBinder<F extends Facet> {

    private final Binder binder;
    private final TypeLiteral<F> baseFacetType;
    private final Multibinder<F> facetBinder;

    protected FacetBinder(Binder binder) {
        this.binder = binder;
        this.baseFacetType = new ResolvableType<F>(){}.in(getClass());
        this.facetBinder = Multibinder.newSetBinder(binder, baseFacetType);
    }

    /**
     * Register the given facet type
     */
    public void add(Class<? extends F> facetType) {
        facetBinder.addBinding().to(facetType);
    }

    /**
     * Register the given facet type, AND self-bind it in the given scope, defaulting to {@link InjectorScoped}
     */
    public void register(Class<? extends F> facetType, @Nullable Class<? extends Annotation> scope) {
        add(facetType);
        binder.bind(facetType).in(Optionals.first(Optional.ofNullable(scope),
                                                  Injection.scopeAnnotation(facetType))
                                           .orElse(InjectorScoped.class));
    }

    /**
     * Register the given facet type, AND self-bind it as {@link InjectorScoped}
     */
    public void register(Class<? extends F> facetType) {
        register(facetType, null);
    }
}
