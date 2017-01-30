package tc.oc.pgm.raindrops;

import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.pgm.match.inject.MatchBinders;
import tc.oc.pgm.match.inject.MatchScoped;

public class RaindropManifest extends HybridManifest implements MatchBinders {
    @Override
    protected void configure() {
        bind(RaindropListener.class).in(MatchScoped.class);
        matchListener(RaindropListener.class);
    }
}
