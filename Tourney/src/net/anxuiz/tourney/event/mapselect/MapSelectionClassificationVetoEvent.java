package net.anxuiz.tourney.event.mapselect;

import net.anxuiz.tourney.vote.VetoVote;
import org.bukkit.event.HandlerList;
import tc.oc.api.docs.Entrant;
import tc.oc.pgm.teams.Team;

public class MapSelectionClassificationVetoEvent extends MapSelectionTeamEvent {
    private static final HandlerList handlers = new HandlerList();

    public MapSelectionClassificationVetoEvent(final VetoVote vote, final Team team, final Entrant entrant) {
        super(vote, team, entrant);
    }

    public static HandlerList getHandlerList() {
        return MapSelectionClassificationVetoEvent.handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return MapSelectionClassificationVetoEvent.handlers;
    }
}
