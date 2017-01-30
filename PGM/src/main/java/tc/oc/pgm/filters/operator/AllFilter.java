package tc.oc.pgm.filters.operator;

import java.util.Arrays;

import com.google.common.collect.Iterables;
import tc.oc.commons.core.IterableUtils;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.matcher.StaticFilter;
import tc.oc.pgm.filters.query.IQuery;

public class AllFilter extends AggregateFilter {

    public AllFilter(Iterable<? extends Filter> filters) {
        super(filters);
    }

    public AllFilter(Filter... filters) {
        this(Arrays.asList(filters));
    }

    @Override
    public QueryResponse query(IQuery query) {
        // returns true if all the filters match
        QueryResponse response = QueryResponse.ABSTAIN;
        for(Filter filter : this.filters) {
            QueryResponse filterResponse = filter.query(query);
            if(filterResponse == QueryResponse.DENY) {
                return filterResponse;
            } else if(filterResponse == QueryResponse.ALLOW) {
                response = filterResponse;
            }
        }
        return response;
    }

    public static Filter of(Filter... filters) {
        return of(Arrays.asList(filters));
    }

    public static Filter of(Iterable<? extends Filter> filters) {
        return IterableUtils.unify(Iterables.filter(filters, filter -> !StaticFilter.ABSTAIN.equals(filter)), StaticFilter.ALLOW, AllFilter::new);
    }
}
