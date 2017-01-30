package tc.oc.pgm.filters.operator;

import java.util.Optional;

import tc.oc.pgm.filters.matcher.TypedFilter;
import tc.oc.pgm.filters.matcher.party.CompetitorFilter;
import tc.oc.pgm.filters.query.IMatchQuery;
import tc.oc.pgm.teams.TeamFactory;
import tc.oc.pgm.teams.TeamMatchModule;

/**
 * Adapts a {@link CompetitorFilter}, which depends on the party in the query,
 * into a filter with an explicit {@link TeamFactory} that can respond to any
 * {@link IMatchQuery}.
 *
 * The team can also be omitted, in which case this delegates to
 * {@link CompetitorFilter#matchesAny(IMatchQuery)}.
 *
 * Note that this is not a {@link SingleFilterFunction}, because it is (currently)
 * entirely transparent to the user. That is, it cannot be created directly
 * through XML, it is only used to implement other filters.
 *
 * As a future enhancement, we could potentially allow it to be created directly,
 * which might look something like this:
 *
 *     <team team="red-team">
 *         <score>5</score>
 *     </team>
 */
public class TeamFilterAdapter extends TypedFilter.Impl<IMatchQuery> {

    private final @Inspect Optional<TeamFactory> team;
    private final @Inspect CompetitorFilter filter;

    public TeamFilterAdapter(Optional<TeamFactory> team, CompetitorFilter filter) {
        this.team = team;
        this.filter = filter;
    }

    @Override
    public String toString() {
        return inspect();
    }

    @Override
    public boolean isDynamic() {
        return filter.isDynamic();
    }

    @Override
    public boolean matches(IMatchQuery query) {
        return team.isPresent() ? query.module(TeamMatchModule.class)
                                       .map(tmm -> filter.matches(query, tmm.team(team.get())))
                                       .orElse(false)
                                : filter.matchesAny(query);
    }
}
