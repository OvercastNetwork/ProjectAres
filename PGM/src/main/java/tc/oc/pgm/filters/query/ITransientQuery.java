package tc.oc.pgm.filters.query;

import tc.oc.commons.core.random.Entropy;
import tc.oc.commons.core.random.SaltedEntropy;
import tc.oc.pgm.match.Match;

/**
 * A time-sensitive query, representing some instantaneous match event
 *
 * @see IEventQuery
 */
public interface ITransientQuery extends IMatchQuery {

    /**
     * Return a per-tick {@link Entropy} that generates values
     * unique to this query.
     *
     * @see Match#entropyForTick()
     */
    default Entropy entropy() {
        return new SaltedEntropy(getMatch().entropyForTick(), randomSeed());
    }
}
