package tc.oc.pgm.tutorial;

import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.pgm.map.inject.MapBinders;
import tc.oc.pgm.match.MatchPlayerFacetBinder;
import tc.oc.pgm.match.inject.MatchBinders;

public class TutorialManifest extends HybridManifest implements MapBinders, MatchBinders {
    @Override
    protected void configure() {
        bindRootElementParser(Tutorial.class)
            .to(TutorialParser.class);

        installPlayerModule(binder -> {
            new MatchPlayerFacetBinder(binder).register(TutorialPlayerFacet.class);
        });
    }
}
