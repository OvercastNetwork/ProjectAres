package tc.oc.pgm.menu;

import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.pgm.match.inject.MatchBinders;
import tc.oc.pgm.match.inject.MatchScoped;
import tc.oc.pgm.tokens.TokenListener;

public class MenuManifest extends HybridManifest implements MatchBinders {
    @Override
    protected void configure() {
        bind(MenuListener.class).in(MatchScoped.class);
        matchListener(MenuListener.class);
    }
}
