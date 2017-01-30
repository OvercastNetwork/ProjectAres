package tc.oc.pgm.itemkeep;

import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.pgm.map.inject.MapBinders;
import tc.oc.pgm.match.MatchPlayerFacetBinder;
import tc.oc.pgm.match.inject.MatchBinders;

public class ItemKeepManifest extends HybridManifest implements MapBinders, MatchBinders {
    @Override
    protected void configure() {
        bindRootElementParser(ItemKeepRules.class)
            .to(ItemKeepParser.class);

        installPlayerModule(binder -> {
            new MatchPlayerFacetBinder(binder)
                .register(ItemKeepPlayerFacet.class);
        });
    }
}

