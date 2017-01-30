package tc.oc.pgm.filters.operator;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tc.oc.commons.core.util.Streams;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.matcher.StaticFilter;

/**
 * A filter that derives its response from the responses of multiple
 * child filters to the same query.
 *
 * @see AggregateFilter which has stronger requirements
 * @see SingleFilterFunction which operates on a single child filter
 */
public abstract class MultiFilterFunction extends Filter.Impl {
    @Inspect protected final List<Filter> filters;

    public MultiFilterFunction(Iterable<? extends Filter> filters) {
        this.filters = Streams.of(filters)
                              .filter(f -> !f.equals(StaticFilter.ABSTAIN))
                              .collect(Collectors.toList());
    }

    @Override
    public Stream<? extends Filter> dependencies() {
        return filters.stream();
    }

    @Override
    public String toString() {
        return inspect();
    }
}
