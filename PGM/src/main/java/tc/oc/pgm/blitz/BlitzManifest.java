package tc.oc.pgm.blitz;

import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.pgm.map.inject.MapBinders;
import tc.oc.pgm.match.inject.MatchBinders;
import tc.oc.pgm.match.inject.MatchModuleFixtureManifest;

public class BlitzManifest extends HybridManifest implements MapBinders, MatchBinders {

    @Override
    protected void configure() {
        bindRootElementParser(BlitzProperties.class).to(BlitzParser.class);
        bind(BlitzMatchModule.class).to(BlitzMatchModuleImpl.class);
        install(new MatchModuleFixtureManifest<BlitzMatchModuleImpl>(){});
    }

}
