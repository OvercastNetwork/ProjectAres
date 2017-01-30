package tc.oc.pgm.structure;

import tc.oc.commons.core.inject.Manifest;
import tc.oc.pgm.map.inject.MapBinders;
import tc.oc.pgm.map.inject.MapScoped;
import tc.oc.pgm.match.inject.MatchScoped;

public class StructureManifest extends Manifest implements MapBinders {
    @Override
    protected void configure() {
        installInnerClassFactory(DynamicDefinitionImpl.DynamicImpl.class);
        installFactory(DynamicDefinitionImpl.Factory.class);

        bind(StructureParser.class).in(MapScoped.class);
        rootParsers().addBinding().to(StructureParser.class);

        bind(DynamicScheduler.class).in(MatchScoped.class);
    }
}
