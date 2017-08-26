package tc.oc.pgm.tokens;

import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.pgm.match.inject.MatchBinders;
import tc.oc.pgm.match.inject.MatchScoped;

public class TokenManifest extends HybridManifest implements MatchBinders {
    @Override
    protected void configure() {
        bind(TokenListener.class).in(MatchScoped.class);
        matchListener(TokenListener.class);
    }
}
