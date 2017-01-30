package tc.oc.pgm.filters.operator;

import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.FilterTypeException;
import tc.oc.pgm.filters.query.IQuery;

import static tc.oc.commons.core.exception.LambdaExceptionUtils.rethrowConsumer;

/**
 * A {@link MultiFilterFunction} supporting dynamics.
 *
 * Filters extending this class have the extra constraint that
 * they must respond to any query type that all of their child
 * filters respond to, unless there are no child filters, in
 * which case {@link #respondsTo(Class)} and {@link #isDynamic()}
 * always return false.
 *
 * TODO: It would be better if the exception for empty children was
 * not necessary, but for backwards compatibility, the logical
 * filter operators abstain if they are empty.
 */
public abstract class AggregateFilter extends MultiFilterFunction {

    public AggregateFilter(Iterable<? extends Filter> filters) {
        super(filters);
    }

    @Override
    public boolean respondsTo(Class<? extends IQuery> queryType) {
        return !filters.isEmpty() &&
               filters.stream().allMatch(f -> f.respondsTo(queryType));
    }

    @Override
    public void assertRespondsTo(Class<? extends IQuery> queryType) throws FilterTypeException {
        if(filters.isEmpty()) {
            throw new FilterTypeException(this, queryType);
        }
        filters.forEach(rethrowConsumer(f -> f.assertRespondsTo(queryType)));
    }

    @Override
    public boolean isDynamic() {
        return !filters.isEmpty();
    }
}
