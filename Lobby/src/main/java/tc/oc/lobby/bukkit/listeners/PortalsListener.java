package tc.oc.lobby.bukkit.listeners;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import tc.oc.commons.bukkit.teleport.Navigator;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.commons.core.scheduler.Scheduler;
import tc.oc.lobby.bukkit.portals.Portal;
import tc.oc.lobby.bukkit.portals.PortalsConfig;

public class PortalsListener implements PluginFacet, Listener {

    private final Set<Portal> portals = new HashSet();
    private final Set<UUID> connecting = Sets.newConcurrentHashSet();
    private PortalsConfig config;
    private Scheduler scheduler;

    @Inject
    PortalsListener(PortalsConfig config, Scheduler scheduler) {
        this.config = config;
        this.scheduler = scheduler;
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
                UUID uuid = event.getPlayer().getUniqueId();
                if(connector.isConnectable() && !connecting.contains(uuid)) {
                    connector.teleport(event.getPlayer());
                    connecting.add(uuid);
                    scheduler.createDelayedTask(Duration.ofSeconds(5), () -> connecting.remove(uuid));
                    break;
                }
            }
        }
    }
}
