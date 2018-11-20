package tc.oc.lobby.bukkit;

import com.google.inject.Provides;
import org.bukkit.Server;
import org.bukkit.World;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.plugin.PluginFacetBinder;
import tc.oc.lobby.bukkit.gizmos.GizmoUtils;
import tc.oc.lobby.bukkit.gizmos.gun.GunGizmo;
import tc.oc.lobby.bukkit.gizmos.halloween.ghost.GhostGizmo;
import tc.oc.lobby.bukkit.listeners.PlayerListener;
import tc.oc.lobby.bukkit.listeners.PortalsListener;
import tc.oc.lobby.bukkit.listeners.RaindropsListener;

public class LobbyManifest extends HybridManifest {
    @Override
    protected void configure() {
        expose(SignUpdater.class);

        final PluginFacetBinder facets = new PluginFacetBinder(binder());
        facets.register(PlayerListener.class);
        facets.register(SignUpdater.class);
        facets.register(RaindropsListener.class);
        facets.register(PortalsListener.class);

        requestStaticInjection(GizmoUtils.class);
        requestStaticInjection(GunGizmo.class);
        requestStaticInjection(GhostGizmo.class);
    }

    @Provides World world(Server server) {
        return server.getWorlds().get(0);
    }
}
