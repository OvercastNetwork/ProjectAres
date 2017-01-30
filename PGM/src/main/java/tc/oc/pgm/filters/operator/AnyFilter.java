package tc.oc.pgm.filters.operator;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tc.oc.commons.core.IterableUtils;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.matcher.StaticFilter;
import tc.oc.pgm.filters.query.IQuery;

public class AnyFilter extends AggregateFilter {

    public AnyFilter(Iterable<? extends Filter> filters) {
        super(filters);
    }

    public AnyFilter(Filter... filters) {
        this(Arrays.asList(filters));
    }

    @Override
    public QueryResponse query(IQuery query) {
        // returns true if any of the filters match
        QueryResponse response = QueryResponse.ABSTAIN;
        for(Filter filter : this.filters) {
            QueryResponse filterResponse = filter.query(query);
            if(filterResponse == QueryResponse.ALLOW) {
                return filterResponse;
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
        return IterableUtils.unify(filters, StaticFilter.DENY, AnyFilter::new);
    }

    public static Filter of(Stream<? extends Filter> filters) {
        return of(filters.collect(Collectors.toList()));
    }
}
