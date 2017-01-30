package tc.oc.pgm.filters.matcher;

import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.query.IQuery;

/**
 * A filter that NEVER responds to queries outside of {@link #queryType()},
 * and ALWAYS responds to queries extending {@link #queryType()}.
 *
 * Queries of the latter type are passed to {@link #matches(IQuery)}.
 */
public interface TypedFilter<Q extends IQuery> extends WeakTypedFilter<Q> {

    @Override
    default boolean respondsTo(Class<? extends IQuery> queryType) {
        return queryType().isAssignableFrom(queryType);
    }

    default QueryResponse queryTyped(Q query) {
        return QueryResponse.fromBoolean(matches(query));
    }

    boolean matches(Q query);

    abstract class Impl<Q extends IQuery> extends Filter.Impl implements TypedFilter<Q> {}
}
