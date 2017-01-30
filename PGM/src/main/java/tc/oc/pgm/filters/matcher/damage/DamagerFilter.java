package tc.oc.pgm.filters.matcher.damage;

import java.util.Optional;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.operator.TransformedFilter;
import tc.oc.pgm.filters.query.IDamageQuery;
import tc.oc.pgm.filters.query.IEntityEventQuery;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.tracker.damage.EntityInfo;

public class DamagerFilter extends TransformedFilter<IDamageQuery, IEntityEventQuery> {

    public DamagerFilter(Filter filter) {
        super(filter);
    }

    @Override
    protected Optional<IEntityEventQuery> transformQuery(IDamageQuery query) {
        return query.getDamageInfo()
                    .damager()
                    .filter(damager -> damager instanceof EntityInfo)
                    .map(damager -> new IEntityEventQuery() {
                        @Override
                        public Class<? extends Entity> getEntityType() {
                            return ((EntityInfo) damager).getEntityClass();
                        }

                        @Override
                        public Event getEvent() {
                            return query.getEvent();
                        }

                        @Override
                        public Match getMatch() {
                            return query.getMatch();
                        }
                    });
    }
}
