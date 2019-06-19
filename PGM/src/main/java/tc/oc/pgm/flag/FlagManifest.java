package tc.oc.pgm.flag;

import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.pgm.map.inject.MapBinders;
import tc.oc.pgm.match.MatchPlayerFacetBinder;
import tc.oc.pgm.match.inject.MatchBinders;

public class FlagManifest extends HybridManifest implements MapBinders, MatchBinders {

    @Override
    protected void configure() {
        rootParsers().addBinding().to(FlagParser.class);

        installPlayerModule(binder -> {
            final MatchPlayerFacetBinder facets = new MatchPlayerFacetBinder(binder);
            facets.register(LegacyFlagPlayerFacet.class);
        });
    }

}
