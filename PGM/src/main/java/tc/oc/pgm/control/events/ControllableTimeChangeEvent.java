package tc.oc.pgm.control.events;

import org.bukkit.event.HandlerList;
import tc.oc.pgm.control.ControllableGoal;
import tc.oc.pgm.match.Match;

public class ControllableTimeChangeEvent extends ControllableGoalEvent {

    public ControllableTimeChangeEvent(Match match, ControllableGoal controllable) {
        super(match, controllable);
    }

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
