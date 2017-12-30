package tc.oc.pgm.control.events;

import org.bukkit.event.HandlerList;
import tc.oc.pgm.control.ControllableGoal;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;

public class ControllableTeamChangeEvent extends ControllableGoalEvent {

    private final Competitor oldTeam;
    private final Competitor newTeam;

    public ControllableTeamChangeEvent(Match match, ControllableGoal controllable, Competitor oldTeam, Competitor newTeam) {
        super(match, controllable);
        this.oldTeam = oldTeam;
        this.newTeam = newTeam;
    }

    public Competitor oldTeam() {
        return oldTeam;
    }

    public Competitor newTeam() {
        return newTeam;
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
