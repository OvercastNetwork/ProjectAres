package tc.oc.commons.bungee.listeners;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;
import javax.inject.Inject;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.message.MessageListener;
import tc.oc.api.message.MessageQueue;
import tc.oc.api.message.types.PlayerTeleportRequest;
import tc.oc.api.model.ModelSync;
import tc.oc.commons.bungee.servers.ServerTracker;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.plugin.PluginFacet;

/**
 * Handles remote teleport requests for players on servers that are not running Commons
 */
public class TeleportListener implements MessageListener, PluginFacet {

    private final ProxyServer proxy;
    private final Logger logger;
    private final ServerTracker serverTracker;
    private final MessageQueue primaryQueue;
    private final ExecutorService executor;

    @Inject TeleportListener(Loggers loggers, ProxyServer proxy, ServerTracker serverTracker, MessageQueue primaryQueue, @ModelSync ExecutorService executor) {
        this.proxy = proxy;
        this.executor = executor;
        this.logger = loggers.get(getClass());
        this.serverTracker = serverTracker;
        this.primaryQueue = primaryQueue;
    }

    @Override
    public void enable() {
        primaryQueue.subscribe(this, executor);
        primaryQueue.bind(PlayerTeleportRequest.class);
    }

    @Override
    public void disable() {
        primaryQueue.unsubscribe(this);
    }

    @HandleMessage
    public void onTeleport(PlayerTeleportRequest message) {
        final ProxiedPlayer player = proxy.getPlayer(message.player_uuid);
        if(player == null) return;

        serverTracker.serverInfo(message.target_server()).ifPresent(targetServerInfo -> {
            final Server server = serverTracker.byPlayer(player);
            if(server.role() == ServerDoc.Role.LOBBY || server.role() == ServerDoc.Role.PGM) {
                // If Bukkit server is running Commons, let it handle the teleport
                return;
            }

            if(!Objects.equals(player.getServer().getInfo(), targetServerInfo)) {
                logger.info("Remote teleporting " + player.getName() + " to " + targetServerInfo.getName() + ":" + message.target_player_uuid);
                player.connect(targetServerInfo);
            }
        });
    }
}
