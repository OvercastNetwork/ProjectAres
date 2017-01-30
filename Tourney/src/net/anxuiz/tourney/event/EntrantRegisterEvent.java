package net.anxuiz.tourney.event;

import com.google.common.base.Preconditions;
import org.bukkit.event.HandlerList;
import tc.oc.api.docs.Entrant;
import tc.oc.pgm.events.MatchEvent;
import tc.oc.pgm.teams.Team;

/**
 * Called AFTER a tournament team is registered to a PGM team
 */
public class EntrantRegisterEvent extends MatchEvent {
    private static final HandlerList handlers = new HandlerList();
    private final Team team;
    private final Entrant entrant;

    public EntrantRegisterEvent(final Team team, final Entrant entrant) {
        super(team.getMatch());
        this.team = Preconditions.checkNotNull(team, "Team");
        this.entrant = Preconditions.checkNotNull(entrant, "Entrant");
    }

    public static HandlerList getHandlerList() {
        return EntrantRegisterEvent.handlers;
    }

    public Entrant getEntrant() {
        return entrant;
    }

    public Team getTeam() {
        return team;
    }

    @Override
    public HandlerList getHandlers() {
        return EntrantRegisterEvent.handlers;
    }
}
