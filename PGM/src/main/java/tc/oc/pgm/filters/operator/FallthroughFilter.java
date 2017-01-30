package tc.oc.pgm.filters.operator;

import java.util.Arrays;

import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.matcher.StaticFilter;
import tc.oc.pgm.filters.query.IQuery;

/**
 * Return a fixed response if any child filter returns ALLOW, otherwise return ABSTAIN.
 */
public class FallthroughFilter extends MultiFilterFunction {

    private final QueryResponse response;

    public FallthroughFilter(QueryResponse response, Filter... filters) {
        this(response, Arrays.asList(filters));
    }

    public FallthroughFilter(QueryResponse response, Iterable<? extends Filter> filters) {
        super(filters);
        this.response = response;
    }

    public static Filter of(QueryResponse response, Filter... filters) {
        return of(response, Arrays.asList(filters));
    }

    public static Filter of(QueryResponse response, Iterable<? extends Filter> filters) {
        if(filters.iterator().hasNext()) {
            return new FallthroughFilter(response, filters);
        } else {
            return StaticFilter.ABSTAIN;
        }
    }

    public static Filter allow(Iterable<? extends Filter> filters) {
        return of(QueryResponse.ALLOW, filters);
    }

    public static Filter deny(Iterable<? extends Filter> filters) {
        return of(QueryResponse.DENY, filters);
    }

    public static Filter deny(Filter... filters) {
        return deny(Arrays.asList(filters));
    }

    @Override
    public boolean respondsTo(Class<? extends IQuery> queryType) {
        return false;
    }

    @Override
    public QueryResponse query(IQuery query) {
        for(Filter filter : filters) {
            if(filter.query(query) == QueryResponse.ALLOW) return response;
        }
        return QueryResponse.ABSTAIN;
    }
}
