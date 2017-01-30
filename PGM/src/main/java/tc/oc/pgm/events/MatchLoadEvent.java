package tc.oc.pgm.events;

import tc.oc.pgm.match.Match;

/**
 * Fired after a {@link Match} has completely loaded but before any players have joined
 */
public class MatchLoadEvent extends MatchStateChangeEvent {
    public MatchLoadEvent(Match match) {
        super(match, null, match.matchState());
    }
}
