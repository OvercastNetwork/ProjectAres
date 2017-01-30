package tc.oc.pgm.filters.matcher.damage;

import java.util.Optional;

import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.operator.TransformedFilter;
import tc.oc.pgm.filters.query.IDamageQuery;
import tc.oc.pgm.filters.query.PlayerEventQuery;

public class AttackerFilter extends TransformedFilter<IDamageQuery, PlayerEventQuery> {

    public AttackerFilter(Filter child) {
        super(child);
    }

    @Override
    protected Optional<PlayerEventQuery> transformQuery(IDamageQuery query) {
        return query.getDamageInfo()
                    .attacker()
                    .map(attacker -> new PlayerEventQuery(attacker, query.getEvent()));
    }
}
