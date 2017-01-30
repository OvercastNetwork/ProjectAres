package tc.oc.pgm.flag;

import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.pgm.map.inject.MapBinders;

public class FlagManifest extends HybridManifest implements MapBinders {
    @Override
    protected void configure() {
        rootParsers().addBinding().to(FlagParser.class);
    }
}
