package tc.oc.pgm.events;

import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchState;

public class MatchBeginEvent extends MatchStateChangeEvent {
    public MatchBeginEvent(Match match, MatchState oldState) {
        super(match, oldState, MatchState.Running);
    }
}
