package tc.oc.pgm.filters.operator;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import tc.oc.commons.core.IterableUtils;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.matcher.StaticFilter;
import tc.oc.pgm.filters.query.IQuery;

/**
 * Returns the result of the first child filter that does not abstain
 */
public class ChainFilter extends MultiFilterFunction {

    public ChainFilter(Iterable<? extends Filter> filters) {
        super(filters);
    }

    public static Filter forward(List<? extends Filter> filters) {
        return IterableUtils.<Filter>unify(filters, StaticFilter.ABSTAIN, ChainFilter::new);
    }

    /**
     * Return a reversed chain, so later filters have priority over earlier ones
     */
    public static Filter reverse(List<? extends Filter> filters) {
        return IterableUtils.<Filter>unify(filters, StaticFilter.ABSTAIN, multi -> new ChainFilter(Lists.reverse(ImmutableList.copyOf(multi))));
    }

    @Override
    public boolean respondsTo(Class<? extends IQuery> queryType) {
        return false;
    }

    @Override
    public QueryResponse query(IQuery query) {
        for(Filter filter : filters) {
            final QueryResponse response = filter.query(query);
            if(response.isPresent()) return response;
        }
        return QueryResponse.ABSTAIN;
    }
}
