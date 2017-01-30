package tc.oc.pgm.filters.matcher.match;

import com.google.common.collect.Range;
import tc.oc.pgm.filters.matcher.TypedFilter;
import tc.oc.pgm.filters.query.ITransientQuery;

/**
 * Return a pseudo-random result derived from the query and current tick.
 */
public class RandomFilter extends TypedFilter.Impl<ITransientQuery> {

    private final @Inspect Range<Double> chance;

    public RandomFilter(Range<Double> chance) {
        this.chance = chance;
    }

    @Override
    public boolean matches(ITransientQuery query) {
        return chance.contains(query.entropy().randomDouble());
    }
}
