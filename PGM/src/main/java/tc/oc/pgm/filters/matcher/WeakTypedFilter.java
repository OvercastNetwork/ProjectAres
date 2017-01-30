package tc.oc.pgm.filters.matcher;

import tc.oc.commons.core.reflect.TypeParameterCache;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.query.IQuery;

/**
 * A filter that NEVER responds to queries outside of {@link #queryType()},
 * and SOMETIMES responds to queries extending {@link #queryType()}.
 *
 * Queries of the latter type are passed to {@link #queryTyped(IQuery)}.
 *
 * The runtime type of {@link Q} is detected automatically if it
 * is specified by a subclass.
 */
public interface WeakTypedFilter<Q extends IQuery> extends Filter {

    TypeParameterCache<WeakTypedFilter, ? extends IQuery> Q_CACHE = new TypeParameterCache<>(WeakTypedFilter.class, "Q");

    default Class<? extends Q> queryType() {
        return (Class<Q>) Q_CACHE.resolveRaw(getClass());
    }

    default QueryResponse query(IQuery query) {
        return queryType().isInstance(query) ? queryTyped((Q) query)
                                             : QueryResponse.ABSTAIN;
    }

    QueryResponse queryTyped(Q query);
}
