package tc.oc.pgm.legacy;

import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.pgm.match.MatchPlayerFacetBinder;
import tc.oc.pgm.match.inject.MatchBinders;

public class LegacyManifest extends HybridManifest implements MatchBinders {

    @Override
    protected void configure() {
        installPlayerModule(binder -> {
            final MatchPlayerFacetBinder facets = new MatchPlayerFacetBinder(binder);
            facets.register(LegacyPlayerFacet.class);
        });
    }

}
