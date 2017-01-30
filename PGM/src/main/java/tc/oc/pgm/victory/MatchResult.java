package tc.oc.pgm.victory;

import java.util.Comparator;

import net.md_5.bungee.api.chat.BaseComponent;
import tc.oc.pgm.match.Competitor;

/**
 * Orders {@link Competitor}s by how close they are to winning the match
 */
public interface MatchResult extends Comparator<Competitor> {
    /**
     * Return a short (one line) description of this type of result.
     */
    BaseComponent describeResult();

    /**
     * If true, ties cannot be resolved by other results.
     */
    default boolean isDefinite() {
        return false;
    }
}
