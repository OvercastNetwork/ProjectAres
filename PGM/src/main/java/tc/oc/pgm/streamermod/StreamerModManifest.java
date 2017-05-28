package tc.oc.pgm.streamermod;

import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.pgm.map.inject.MapBinders;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.inject.MatchBinders;
import tc.oc.pgm.match.inject.MatchScoped;

public class StreamerModManifest extends HybridManifest implements MapBinders, MatchBinders {
    @Override
    protected void configure() {
        bind(StreamerModListener.class).in(MatchScoped.class);
        matchListener(StreamerModListener.class, MatchScope.LOADED);
    }
}

