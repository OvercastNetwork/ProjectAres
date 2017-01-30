package net.anxuiz.tourney.event.mapselect;

import net.anxuiz.tourney.vote.VetoVote;
import org.bukkit.event.HandlerList;
import tc.oc.api.docs.Entrant;
import tc.oc.pgm.teams.Team;

public class MapSelectionMapVetoEvent extends MapSelectionTeamEvent {
    private static final HandlerList handlers = new HandlerList();

    public MapSelectionMapVetoEvent(final VetoVote vote, final Team team, final Entrant entrant) {
        super(vote, team, entrant);
    }

    public static HandlerList getHandlerList() {
        return MapSelectionMapVetoEvent.handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return MapSelectionMapVetoEvent.handlers;
    }
}
