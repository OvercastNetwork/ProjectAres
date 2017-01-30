package tc.oc.pgm.loot;

import tc.oc.pgm.features.FeatureDefinition;
import tc.oc.pgm.features.FeatureInfo;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.matcher.StaticFilter;
import tc.oc.pgm.regions.BlockBoundedValidation;
import tc.oc.pgm.regions.Region;

@FeatureInfo(name = "cache", plural = "lootables", singular = "cache")
public interface Cache extends FeatureDefinition {

    @Property
    @Validate(BlockBoundedValidation.class)
    Region region();

    @Property
    default Filter filter() {
        return StaticFilter.ALLOW;
    }
}
