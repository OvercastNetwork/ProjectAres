package tc.oc.pgm.events;

import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchState;

public class MatchEndEvent extends MatchStateChangeEvent {
    public MatchEndEvent(Match match) {
        super(match, MatchState.Running, MatchState.Finished);
    }
}
