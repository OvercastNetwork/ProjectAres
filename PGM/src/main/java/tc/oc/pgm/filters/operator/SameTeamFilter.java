package tc.oc.pgm.filters.operator;

import java.util.Optional;

import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.query.IPartyQuery;
import tc.oc.pgm.filters.query.IPlayerQuery;

/**
 * Transforms a player query into a query on their team.
 */
public class SameTeamFilter extends TransformedFilter<IPartyQuery, IPartyQuery> {

    public SameTeamFilter(Filter child) {
        super(child);
    }

    @Override
    protected Optional<IPartyQuery> transformQuery(IPartyQuery query) {
        return Optional.of(query instanceof IPlayerQuery ? query.getParty()
                                                         : query);
    }
}
