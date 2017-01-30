package tc.oc.pgm.filters.matcher.match;

import com.google.common.collect.Range;
import tc.oc.commons.core.random.SaltedEntropy;
import tc.oc.pgm.filters.matcher.TypedFilter;
import tc.oc.pgm.filters.query.IMatchQuery;

/**
 * Random filter that responds to non-event queries, which exposes a lot of undefined behavior.
 *
 * Removed in proto 1.4.1
 */
public class LegacyRandomFilter extends TypedFilter.Impl<IMatchQuery> {

    private final @Inspect Range<Double> chance;

    public LegacyRandomFilter(Range<Double> chance) {
        this.chance = chance;
    }

    @Override
    public boolean matches(IMatchQuery query) {
        return chance.contains(new SaltedEntropy(query.getMatch().entropyForTick(),
                                                 query.randomSeed())
                                   .randomDouble());
    }
}
