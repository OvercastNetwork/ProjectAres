package tc.oc.pgm.highlights;

import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.pgm.map.inject.MapBinders;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.inject.MatchBinders;
import tc.oc.pgm.match.inject.MatchScoped;

public class HighlightManifest extends HybridManifest implements MapBinders, MatchBinders {
    @Override
    protected void configure() {
        bind(HighlightListener.class).in(MatchScoped.class);
        matchListener(HighlightListener.class, MatchScope.LOADED);
    }
}

