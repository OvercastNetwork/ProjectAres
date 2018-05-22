package tc.oc.commons.bukkit.teleport;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import tc.oc.api.bukkit.users.OnlinePlayers;
import tc.oc.api.message.MessageListener;
import tc.oc.api.message.MessageQueue;
import tc.oc.api.message.types.PlayerTeleportRequest;
import tc.oc.commons.bukkit.permissions.PermissionRegistry;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.minecraft.api.scheduler.Scheduler;
import tc.oc.minecraft.api.scheduler.Tickable;
import tc.oc.minecraft.scheduler.SyncExecutor;

/**
 * Listens for remote teleport requests and executes both ends of them.
 *
 * When the request target is a different server, this class sends the
 * player there (via Bungee plugin channel). We do this in Bukkit so that
 * it can be cancelled i.e. by PGM when the player is in a match.
 *
 * When the target is this server, and there is also a target player,
 * this class waits for the teleporting player to arrive and then
 * teleports them to the target.
 */
@Singleton
public class TeleportListener implements MessageListener, Listener, PluginFacet, Tickable {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    static class Received {
        final java.time.Instant timestamp = java.time.Instant.now();
        final PlayerTeleportRequest request;

        Received(PlayerTeleportRequest request) {
            this.request = request;
        }
    }

    private final Logger logger;
    private final MessageQueue primaryQueue;
    private final Teleporter teleporter;
    private final SyncExecutor syncExecutor;
    private final Scheduler scheduler;
    private final PermissionRegistry permissionRegistry;
    private final OnlinePlayers onlinePlayers;

    private final Map<UUID, Received> requests = new HashMap<>();

    @Inject TeleportListener(Loggers loggers, MessageQueue primaryQueue, Teleporter teleporter, SyncExecutor syncExecutor, Scheduler scheduler, PermissionRegistry permissionRegistry, OnlinePlayers onlinePlayers) {
        this.logger = loggers.get(getClass());
        this.primaryQueue = primaryQueue;
        this.onlinePlayers = onlinePlayers;
        this.syncExecutor = syncExecutor;
        this.scheduler = scheduler;
        this.permissionRegistry = permissionRegistry;
        this.teleporter = teleporter;
    }

    @Override
    public Duration tickPeriod() {
        return TIMEOUT;
    }

    @Override
    public void enable() {
        permissionRegistry.register(Teleporter.PERMISSION);
        primaryQueue.subscribe(this, syncExecutor);
        primaryQueue.bind(PlayerTeleportRequest.class);
    }

    @Override
    public void disable() {
        primaryQueue.unsubscribe(this);
    }

    @Override
    public void tick() {
        final Instant now = Instant.now();
        requests.values().removeIf(received -> received.timestamp.plus(TIMEOUT).isBefore(now));
    }

    @HandleMessage
    public void onTeleport(PlayerTeleportRequest request) {
        Player traveler = onlinePlayers.find(request.player_uuid);

        if(!teleporter.isLocal(request.target_server())) {
            // Send player to another server
            if(traveler != null && traveler.hasPermission(Teleporter.PERMISSION)) {
                if(request.target_server() == null) {
                    logger.info("Sending " + traveler.getName() + " to lobby");
                } else {
                    logger.info("Sending " + traveler.getName() + " to server " + request.target_server().bungee_name());
                }
                teleporter.remoteTeleport(traveler, request.target_server());
            }
        } else if(request.target_player_uuid != null) {
            // Teleport player to a target on this server
            if(traveler != null) {
                doTeleport(traveler, request);
            } else {
                queueTeleport(request);
            }
        }
    }

    /**
     * Priority must be high enough so that whatever applies observer permissions
     * runs before this, since those include the teleport permission. Lobby does it
     * in PlayerListener and PGM does it in SpawnMatchModule.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Received received = requests.remove(event.getPlayer().getUniqueId());
        if(received != null) {
            doTeleport(event.getPlayer(), received.request);
        }
    }

    private void queueTeleport(final PlayerTeleportRequest request) {
        logger.info("Queueing remote teleport for offline player " + request.player_uuid);
        requests.put(request.player_uuid, new Received(request));

        scheduler.delaySync(TIMEOUT, () -> {
            if(requests.remove(request.player_uuid) != null) {
                logger.warning("Expired remote teleport for " + request.player_uuid);
            }
        });
    }

    private void doTeleport(Player traveler, PlayerTeleportRequest request) {
        teleporter.localTeleport(traveler, request.target_player_uuid);
    }
}
