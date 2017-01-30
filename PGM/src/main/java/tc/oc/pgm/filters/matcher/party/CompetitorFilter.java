package tc.oc.pgm.filters.matcher.party;

import tc.oc.pgm.filters.matcher.TypedFilter;
import tc.oc.pgm.filters.query.IMatchQuery;
import tc.oc.pgm.filters.query.IPartyQuery;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Party;

/**
 * A filter that can be applied to single {@link Competitor}s, or all
 * {@link Competitor}s in the match (in which case it should effectively
 * OR all of the responses).
 *
 * Any other type of {@link Party} is denied.
 */
public abstract class CompetitorFilter extends TypedFilter.Impl<IPartyQuery> {

    /**
     * Does ANY {@link Competitor} match the filter?
     *
     * The base method queries each competitor one by one.
     */
    public boolean matchesAny(IMatchQuery query) {
        return query.competitors()
                    .anyMatch(competitor -> matches(query, competitor));
    }

    /**
     * Respond to the given {@link Competitor}
     */
    public abstract boolean matches(IMatchQuery query, Competitor competitor);

    @Override
    public final boolean matches(IPartyQuery query) {
        return query.getParty() instanceof Competitor && matches(query, (Competitor) query.getParty());
    }
}
