package tc.oc.pgm.control.events;

import org.bukkit.event.HandlerList;
import tc.oc.pgm.control.ControllableGoal;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;

public class ControllableOwnerChangeEvent extends ControllableGoalEvent {

    private final Competitor oldController;
    private final Competitor newController;

    public ControllableOwnerChangeEvent(Match match, ControllableGoal controllable, Competitor oldController, Competitor newController) {
        super(match, controllable);
        this.oldController = oldController;
        this.newController = newController;
    }

    public Competitor oldController() {
        return oldController;
    }

    public Competitor newController() {
        return newController;
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
