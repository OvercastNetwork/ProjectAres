package tc.oc.pgm.filters.query;

import tc.oc.pgm.match.MatchPlayerState;

public class TransientPlayerQuery implements ForwardingPlayerQuery, ITransientQuery {

    private final MatchPlayerState playerState;

    public TransientPlayerQuery(IPlayerQuery delegate) {
        this.playerState = delegate.playerState(); // Capture player state at event time
    }

    @Override
    public IPlayerQuery playerQuery() {
        return playerState;
    }
}
