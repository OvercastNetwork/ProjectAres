package tc.oc.pgm.features;

import tc.oc.pgm.match.Match;

/**
 * Super-class for all match-time features.  Stores a unique ID (see documentation below)
 * that is unique across all elements.  Can be referenced by querying against a
 * {@link Match}'s {@link MatchFeatureContext} after construction-
 * time of the Match.
 */
public interface Feature<T extends FeatureDefinition> {
    /**
     * Return the {@link FeatureDefinition} instance from which this Feature was created
     */
    T getDefinition();

    default boolean isDefinedBy(FeatureDefinition def) {
        return getDefinition().equals(def);
    }
}
