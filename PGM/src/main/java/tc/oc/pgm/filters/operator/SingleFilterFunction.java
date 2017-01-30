package tc.oc.pgm.filters.operator;

import java.util.stream.Stream;

import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.FilterTypeException;
import tc.oc.pgm.filters.query.IQuery;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A filter that forwards queries to a single child filter, and transforms the response in some way.
 *
 * @see MultiFilterFunction which operates on multiple child filters
 * @see TransformedFilter which transforms the query, rather than the response
 */
public abstract class SingleFilterFunction extends Filter.Impl {

    protected final @Inspect Filter filter;

    public SingleFilterFunction(Filter filter) {
        this.filter = checkNotNull(filter, "filter may not be null");
    }

    @Override
    public Stream<? extends Filter> dependencies() {
        return Stream.of(filter);
    }

    @Override
    public boolean respondsTo(Class<? extends IQuery> queryType) {
        return filter.respondsTo(queryType);
    }

    @Override
    public void assertRespondsTo(Class<? extends IQuery> queryType) throws FilterTypeException {
        filter.assertRespondsTo(queryType);
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public String toString() {
        return inspect();
    }
}
