package tc.oc.pgm.control;

import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.pgm.control.point.ControlPointDefinition;
import tc.oc.pgm.control.point.ControlPointParser;
import tc.oc.pgm.control.point.ControlPointRootNodeFinder;
import tc.oc.pgm.features.FeatureBinder;
import tc.oc.pgm.map.inject.MapBinders;
import tc.oc.pgm.map.inject.MapScoped;
import tc.oc.pgm.match.inject.MatchModuleFixtureManifest;

public class ControllableGoalManifest extends HybridManifest implements MapBinders {
    @Override
    protected void configure() {
        bind(ControlPointParser.class).in(MapScoped.class);
        install(new MatchModuleFixtureManifest<ControllableGoalMatchModule>(){});

        final FeatureBinder<ControlPointDefinition> cp = new FeatureBinder<>(binder(), ControlPointDefinition.class);
        cp.bindDefinitionParser().to(ControlPointParser.class);
        cp.installRootParser(new ControlPointRootNodeFinder());
    }
}
