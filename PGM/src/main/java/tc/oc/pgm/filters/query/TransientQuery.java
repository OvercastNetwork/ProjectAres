package tc.oc.pgm.filters.query;

import tc.oc.pgm.match.Match;

public class TransientQuery implements ITransientQuery {

    private final IMatchQuery match;

    public TransientQuery(IMatchQuery match) {
        this.match = match;
    }

    @Override
    public Match getMatch() {
        return match.getMatch();
    }
}
