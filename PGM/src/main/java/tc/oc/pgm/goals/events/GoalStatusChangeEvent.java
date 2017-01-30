package tc.oc.pgm.goals.events;

import tc.oc.pgm.goals.Goal;

public class GoalStatusChangeEvent extends GoalEvent {

    public GoalStatusChangeEvent(Goal goal) {
        super(goal);
    }
}
