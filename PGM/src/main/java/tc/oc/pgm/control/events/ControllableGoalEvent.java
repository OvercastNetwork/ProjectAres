package tc.oc.pgm.control.events;

import tc.oc.pgm.control.ControllableGoal;
import tc.oc.pgm.events.MatchEvent;
import tc.oc.pgm.match.Match;

import java.util.Optional;

public abstract class ControllableGoalEvent extends MatchEvent {

    protected final ControllableGoal controllable;

    public ControllableGoalEvent(Match match, ControllableGoal controllable) {
        super(match);
        this.controllable = controllable;
    }

    public ControllableGoal controllable() {
        return controllable;
    }

    public <T extends ControllableGoal> Optional<ControllableGoal> controllable(Class<T> controllableClass) {
        return Optional.ofNullable(controllableClass.isInstance(controllable) ? controllable : null);
    }

}
