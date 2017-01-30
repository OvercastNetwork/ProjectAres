package tc.oc.pgm.filters.matcher.party;

import tc.oc.pgm.filters.matcher.TypedFilter;
import tc.oc.pgm.filters.query.IPartyQuery;
import tc.oc.pgm.match.Party;
import tc.oc.pgm.teams.Team;
import tc.oc.pgm.teams.TeamFactory;

/**
 * Match the given team, or a player on that team
 */
public class TeamFilter extends TypedFilter.Impl<IPartyQuery> {
    protected final @Inspect(brief=true) TeamFactory team;

    public TeamFilter(TeamFactory team) {
        this.team = team;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public boolean matches(IPartyQuery query) {
        final Party party = query.getParty();
        return party instanceof Team && ((Team) party).isDefinedBy(team);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{team=" + this.team + "}";
    }
}
