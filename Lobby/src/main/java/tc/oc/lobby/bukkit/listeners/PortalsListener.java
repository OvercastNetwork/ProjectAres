package tc.oc.lobby.bukkit.listeners;

import com.google.inject.Inject;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import tc.oc.commons.bukkit.teleport.Navigator;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.lobby.bukkit.portals.Portal;
import tc.oc.lobby.bukkit.portals.PortalsConfig;

import java.util.HashSet;
import java.util.Set;

public class PortalsListener implements PluginFacet, Listener {

    private final Set<Portal> portals = new HashSet();
    private PortalsConfig config;

    @Inject
    PortalsListener(PortalsConfig config) {
        this.config = config;
    }

    public void enable() {
        for (Portal portal : config.loadPortals()) {
            this.portals.add(portal);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void move(PlayerMoveEvent event) {
        for (Portal portal : portals) {
            if (portal.getCuboid().contains(event.getPlayer().getLocation().position())) {
                Navigator.Connector connector = portal.getConnector();
                if(connector.isConnectable()) {
                    connector.teleport(event.getPlayer());
                    break;
                }
            }
        }
    }
}
