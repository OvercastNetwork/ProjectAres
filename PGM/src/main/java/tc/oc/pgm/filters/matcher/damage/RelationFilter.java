package tc.oc.pgm.filters.matcher.damage;

import tc.oc.pgm.filters.matcher.TypedFilter;
import tc.oc.pgm.filters.query.IDamageQuery;
import tc.oc.pgm.match.PlayerRelation;

public class RelationFilter extends TypedFilter.Impl<IDamageQuery> {

    private final @Inspect PlayerRelation relation;

    public RelationFilter(PlayerRelation relation) {
        this.relation = relation;
    }

    @Override
    public boolean matches(IDamageQuery query) {
        return relation.are(query.getVictim(), query.getDamageInfo().getAttacker());
    }
}
