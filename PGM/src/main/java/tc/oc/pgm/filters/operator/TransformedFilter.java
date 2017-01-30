package tc.oc.pgm.filters.operator;

import java.util.Optional;

import tc.oc.commons.core.reflect.TypeParameterCache;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.FilterTypeException;
import tc.oc.pgm.filters.matcher.WeakTypedFilter;
import tc.oc.pgm.filters.query.IQuery;

/**
 * A filter that transforms a query of type {@link Q} into some other query of type {@link R}
 * and passes it to a child filter.
 *
 * @see SingleFilterFunction which transforms the response, rather than the query
 */
public abstract class TransformedFilter<Q extends IQuery, R extends IQuery> extends Filter.Impl implements WeakTypedFilter<Q> {

    private static final TypeParameterCache<TransformedFilter, ? extends IQuery> R_CACHE = new TypeParameterCache<>(TransformedFilter.class, "R");

    private final Class<R> innerQueryType = (Class<R>) R_CACHE.resolveRaw(getClass());
    protected final @Inspect Filter filter;

    public TransformedFilter(Filter filter) {
        this.filter = filter;
    }

    @Override
    public boolean respondsTo(Class<? extends IQuery> queryType) {
        return queryType().isAssignableFrom(queryType) &&
               filter.respondsTo(innerQueryType);
    }

    @Override
    public void assertRespondsTo(Class<? extends IQuery> queryType) throws FilterTypeException {
        if(!queryType().isAssignableFrom(queryType)) {
            throw new FilterTypeException(this, queryType);
        }
        filter.assertRespondsTo(innerQueryType);
    }

    @Override
    public QueryResponse queryTyped(Q query) {
        return transformQuery(query).map(filter::query)
                                    .orElse(QueryResponse.DENY);
    }

    /**
     * Return a transformed query, or empty to DENY
     */
    protected abstract Optional<R> transformQuery(Q query);
}
