package tc.oc.pgm.controlpoint;

import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.pgm.features.FeatureBinder;
import tc.oc.pgm.map.inject.MapBinders;
import tc.oc.pgm.map.inject.MapScoped;

public class ControlPointManifest extends HybridManifest implements MapBinders {
    @Override
    protected void configure() {
        bind(ControlPointParser.class).in(MapScoped.class);

        final FeatureBinder<ControlPointDefinition> cp = new FeatureBinder<>(binder(), ControlPointDefinition.class);
        cp.bindDefinitionParser().to(ControlPointParser.class);
        cp.installMatchModule(ControlPointMatchModule.class);
        cp.installRootParser(new ControlPointRootNodeFinder());
    }
}
