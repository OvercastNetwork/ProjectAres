package tc.oc.pgm.goals.events;

import org.bukkit.event.HandlerList;
import tc.oc.pgm.events.MatchEvent;
import tc.oc.pgm.goals.Goal;

public abstract class GoalEvent<T extends Goal> extends MatchEvent {
    private final T goal;

    protected GoalEvent(T goal) {
        super(goal.getMatch());
        this.goal = goal;
    }

    public T getGoal() {
        return goal;
    }

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
            return handlers;
        }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
