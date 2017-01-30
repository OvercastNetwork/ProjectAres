package net.anxuiz.tourney.event.mapselect;

import com.google.common.base.Preconditions;
import net.anxuiz.tourney.vote.VetoVote;
import tc.oc.api.docs.Entrant;
import tc.oc.pgm.teams.Team;

public abstract class MapSelectionTeamEvent extends MapSelectionEvent {
    private final Team team;
    private final Entrant entrant;

    public MapSelectionTeamEvent(final VetoVote vote, final Team team, Entrant entrant) {
        super(vote);
        this.team = Preconditions.checkNotNull(team, "Team");
        this.entrant = Preconditions.checkNotNull(entrant, "Entrant");
    }

    public Team getTeam() {
        return this.team;
    }

    public Entrant getEntrant() {
        return this.entrant;
    }
}
