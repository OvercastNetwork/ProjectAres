package tc.oc.pgm.terrain;

import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.plugin.PluginFacetBinder;
import tc.oc.pgm.map.inject.MapBinders;
import tc.oc.pgm.map.inject.MapScoped;
import tc.oc.pgm.match.inject.MatchBinders;
import tc.oc.pgm.match.inject.MatchScoped;

public class TerrainManifest extends HybridManifest implements MapBinders, MatchBinders {
    @Override
    protected void configure() {
        bindRootElementParser(TerrainOptions.class)
            .to(TerrainParser.class);

        new WorldConfiguratorBinder(binder())
            .addBinding().to(TerrainOptions.class);

        bind(WorldManager.class)
            .to(WorldManagerImpl.class)
            .in(MapScoped.class);

        new PluginFacetBinder(binder())
            .register(DisableKeepSpawnInMemoryListener.class);

        bind(BlockPhysicsListener.class).in(MatchScoped.class);
        matchListener(BlockPhysicsListener.class);
    }
}
