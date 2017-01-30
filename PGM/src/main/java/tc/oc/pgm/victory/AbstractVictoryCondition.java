package tc.oc.pgm.victory;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractVictoryCondition implements VictoryCondition {

    private final Priority priority;
    private final MatchResult result;

    protected AbstractVictoryCondition(Priority priority, MatchResult result) {
        this.priority = checkNotNull(priority);
        this.result = checkNotNull(result);
    }

    @Override
    public Priority priority() {
        return priority;
    }

    @Override
    public MatchResult result() {
        return result;
    }
}
