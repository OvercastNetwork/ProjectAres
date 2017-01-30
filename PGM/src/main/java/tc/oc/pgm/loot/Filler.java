package tc.oc.pgm.loot;

import java.util.Optional;

import java.time.Duration;
import tc.oc.commons.core.util.TimeUtils;
import tc.oc.pgm.features.FeatureDefinition;
import tc.oc.pgm.features.FeatureInfo;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.matcher.StaticFilter;

@FeatureInfo(name = "fill", plural = "lootables", singular = "fill")
public interface Filler extends FeatureDefinition {

    /**
     * Items to fill with
     */
    @Property Loot loot();

    /**
     * Blocks/entities that are fillable
     */
    @Property default Filter filter() {
        return StaticFilter.ALLOW;
    }

    /**
     * Refill all blocks/entities when this filter goes high
     */
    @Property Optional<Filter> refill_trigger();

    /**
     * Refill an individual block/entity this much time after it was last filled
     */
    @Property default Duration refill_interval() {
        return TimeUtils.INF_POSITIVE;
    }

    /**
     * Clear contents before refilling
     */
    @Property default boolean refill_clear() {
        return true;
    }
}
