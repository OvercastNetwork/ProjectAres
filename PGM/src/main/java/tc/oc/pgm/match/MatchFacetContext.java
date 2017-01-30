package tc.oc.pgm.match;

import javax.inject.Inject;

import tc.oc.commons.bukkit.inject.BukkitFacetContext;
import tc.oc.commons.core.inject.Facet;

public class MatchFacetContext<F extends Facet> extends BukkitFacetContext<F> {

    private @Inject Match match;

    @Override
    protected void enableFacet(F facet) throws Exception {
        super.enableFacet(facet);
        match.registerRepeatable(facet);
    }

    @Override
    protected void disableFacet(F facet) throws Exception {
        match.unregisterRepeatable(facet);
        super.disableFacet(facet);
    }
}
