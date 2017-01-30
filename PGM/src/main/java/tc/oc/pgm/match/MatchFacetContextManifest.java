package tc.oc.pgm.match;

import com.google.inject.TypeLiteral;
import tc.oc.commons.bukkit.inject.BukkitFacetContext;
import tc.oc.commons.core.inject.Facet;
import tc.oc.commons.core.inject.FacetContext;
import tc.oc.commons.core.inject.KeyedManifest;
import tc.oc.commons.core.reflect.ResolvableType;
import tc.oc.commons.core.reflect.TypeArgument;

/**
 * Creates bindings for the {@link FacetContext} hiarchy, down to {@link C}
 */
public class MatchFacetContextManifest<F extends Facet, C extends MatchFacetContext<F>> extends KeyedManifest {

    private final TypeArgument<F> facetType;
    private final Class<C> contextType;

    public MatchFacetContextManifest(Class<F> facetType, Class<C> contextType) {
        this.facetType = new TypeArgument<F>(facetType){};
        this.contextType = contextType;
    }

    @Override
    protected Object manifestKey() {
        return contextType;
    }

    @Override
    protected void configure() {
        final TypeLiteral<FacetContext<F>> fc = new ResolvableType<FacetContext<F>>(){}.with(facetType);
        final TypeLiteral<BukkitFacetContext<F>> bfc = new ResolvableType<BukkitFacetContext<F>>(){}.with(facetType);
        final TypeLiteral<MatchFacetContext<F>> mfc = new ResolvableType<MatchFacetContext<F>>(){}.with(facetType);

        bind(mfc).to(contextType);
        bind(bfc).to(mfc);
        bind(fc).to(bfc);
    }
}
