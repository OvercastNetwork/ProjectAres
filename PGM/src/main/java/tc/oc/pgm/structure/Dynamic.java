package tc.oc.pgm.structure;

import tc.oc.pgm.features.Feature;

public interface Dynamic extends Feature<DynamicDefinition> {
    void place();
    void clear();
}
