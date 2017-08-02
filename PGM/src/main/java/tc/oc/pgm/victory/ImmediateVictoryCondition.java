package tc.oc.pgm.victory;

import tc.oc.pgm.match.Match;

/**
 * Immediately end the match with the given result
 */
public class ImmediateVictoryCondition extends AbstractVictoryCondition {

    public ImmediateVictoryCondition(MatchResult result) {
        super(Priority.IMMEDIATE, result);
    }

    @Override
    public boolean isCompleted() {
        return true;
    }
}
