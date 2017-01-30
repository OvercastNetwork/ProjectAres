package tc.oc.pgm.lane;

import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.pgm.features.FeatureBinder;

public class LaneManifest extends HybridManifest {

    @Override
    protected void configure() {
        final FeatureBinder<Lane> lane = new FeatureBinder<>(binder(), Lane.class);
        lane.installReflectiveParser();
        lane.installRootParser();
        lane.installMatchModule(LaneMatchModule.class);
    }
}
