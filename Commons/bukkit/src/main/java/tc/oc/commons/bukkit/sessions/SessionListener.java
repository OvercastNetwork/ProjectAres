package tc.oc.commons.bukkit.sessions;

import java.net.InetAddress;
import java.util.UUID;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.bukkit.users.OnlinePlayers;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.Session;
import tc.oc.minecraft.protocol.MinecraftVersion;
import tc.oc.minecraft.scheduler.SyncExecutor;
import tc.oc.api.sessions.SessionService;
import tc.oc.api.sessions.SessionStartRequest;
import tc.oc.commons.bukkit.event.UserLoginEvent;
import tc.oc.commons.bukkit.nick.PlayerIdentityChangeEvent;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.commons.core.util.SystemFutureCallback;

/**
 * Adds login sessions to the local cache
 * Finishes sessions when players quit or login is denied
 * Restarts sessions when players change nickname
 */
@Singleton
public class SessionListener implements Listener, PluginFacet {

    private final Logger logger;
    private final SyncExecutor syncExecutor;
    private final Server localServer;
    private final SessionService sessionService;
    private final OnlinePlayers onlinePlayers;
    private final BukkitUserStore userStore;

    @Inject SessionListener(Loggers loggers, SyncExecutor syncExecutor, Server localServer, SessionService sessionService, OnlinePlayers onlinePlayers, BukkitUserStore userStore) {
        this.logger = loggers.get(getClass());
        this.localServer = localServer;
        this.onlinePlayers = onlinePlayers;
        this.userStore = userStore;
        this.syncExecutor = syncExecutor;
        this.sessionService = sessionService;
    }

    private void restartSession(final Player player) {
        final UUID uuid = player.getUniqueId();
        final int entityId = player.getEntityId();
        syncExecutor.callback(
            this.sessionService.start(new SessionStartRequest() {
                @Override
                public String server_id() {
                    return localServer._id();
                }

                @Override
                public String player_id() {
                    return userStore.getUser(player).player_id();
                }

                @Override
                public InetAddress ip() {
                    return player.getAddress().getAddress();
                }

                @Override
                public String version() {
                    return MinecraftVersion.describeProtocol(player.getProtocolVersion());
                }

                @Override
                public @Nullable String previous_session_id() {
                    return userStore.session(player)
                                    .map(Session::_id)
                                    .orElse(null);
                }
            }),
            new SystemFutureCallback<Session>() {
                @Override
                public void onSuccessThrows(Session session) throws Exception {
                    final Player player1 = onlinePlayers.find(uuid);
                    if(player1 != null && player1.getEntityId() == entityId) {
                        // If player is still online, store their session
                        userStore.setSession(player1, session);
                        logger.info("Start session " + session._id() + " for " + player1.getName() + " (identity change)");
                    } else {
                        // If the player disconnected while we were starting their session, finish it right away
                        sessionService.finish(session);
                        logger.info("End session " + session._id() + " for " + player1.getName() + " (quick disconnect)");
                    }
                }
            }
        );
    }

    private void finishSession(Player player) {
        Session session = userStore.removeSession(player);
        if(session != null) {
            this.sessionService.finish(session);
            logger.info("End session " + session._id() + " for " + player.getName());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLoginEarly(UserLoginEvent event) {
        if(event.getSession() != null) {
            userStore.setSession(event.getPlayer(), event.getSession());
            logger.info("Start session " + event.getSession()._id() + " for " + event.getPlayer().getName() + " (login)");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onLoginLate(UserLoginEvent event) {
        if(event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            finishSession(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        finishSession(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNickChange(PlayerIdentityChangeEvent event) {
        restartSession(event.getPlayer());
    }
}
