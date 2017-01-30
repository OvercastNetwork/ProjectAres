package tc.oc.pgm.filters.operator;

import tc.oc.commons.core.IterableUtils;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.matcher.StaticFilter;
import tc.oc.pgm.filters.query.IQuery;

import java.util.Arrays;

public class OneFilter extends AggregateFilter {

    public OneFilter(Iterable<? extends Filter> filters) {
        super(filters);
    }

    public OneFilter(Filter... filters) {
        this(Arrays.asList(filters));
    }

    @Override
    public QueryResponse query(IQuery query) {
        // returns true if exactly one of the filters match
        QueryResponse response = QueryResponse.ABSTAIN;
        for(Filter filter : this.filters) {
            QueryResponse filterResponse = filter.query(query);
            if(filterResponse == QueryResponse.ALLOW) {
                if(response == QueryResponse.ALLOW) {
                    return QueryResponse.DENY;
                } else {
                    response = filterResponse;
                }
            } else if (filterResponse == QueryResponse.DENY) {
                response = filterResponse;
            }
        }
        return response;
    }

    public static Filter of(Filter... filters) {
        return of(Arrays.asList(filters));
    }

    public static Filter of(Iterable<? extends Filter> filters) {
        return IterableUtils.unify(filters, StaticFilter.ABSTAIN, OneFilter::new);
    }
}
