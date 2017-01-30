package tc.oc.commons.bungee.listeners;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.MapMaker;
import com.google.common.util.concurrent.Futures;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.AsyncEvent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import tc.oc.api.bungee.users.BungeeUserStore;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.User;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.minecraft.MinecraftService;
import tc.oc.api.users.LoginRequest;
import tc.oc.api.users.LoginResponse;
import tc.oc.api.users.LogoutRequest;
import tc.oc.api.users.UserService;
import tc.oc.api.util.Permissions;
import tc.oc.commons.bungee.sessions.MojangSessionServiceMonitor;
import tc.oc.commons.bungee.sessions.SessionState;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.commons.core.util.SystemFutureCallback;
import tc.oc.minecraft.protocol.MinecraftVersion;

@Singleton
public class LoginListener implements Listener, PluginFacet {

    private static final String INTERNAL_ERROR = "Internal error\n\nPlease try again later";
    private static final String NOT_ALLOWED = "You are not allowed on this server";

    private final Logger logger;
    private final Plugin plugin;
    private final ProxyServer proxy;
    private final MinecraftService minecraftService;
    private final UserService userService;
    private final BungeeUserStore userStore;
    private final MojangSessionServiceMonitor mojangSessionServiceMonitor;
    private final ConcurrentMap<PendingConnection, User> pendingConnections = new MapMaker().weakKeys().makeMap();
    private final ConcurrentMap<PendingConnection, ServerInfo> initialServers = new MapMaker().weakKeys().makeMap();

    @Inject LoginListener(Loggers loggers, Plugin plugin, ProxyServer proxy, MinecraftService minecraftService, UserService userService, BungeeUserStore userStore, MojangSessionServiceMonitor mojangSessionServiceMonitor) {
        this.proxy = proxy;
        this.mojangSessionServiceMonitor = mojangSessionServiceMonitor;
        this.logger = loggers.get(getClass());
        this.plugin = plugin;
        this.minecraftService = minecraftService;
        this.userService = userService;
        this.userStore = userStore;
    }

    private void log(PendingConnection connection, String message) {
        logger.info("[" + connection.getAddress().getAddress().getHostAddress() +
                    " " + connection.getName() +
                    " " + connection.getUniqueId() +
                    "] " + message);
    }

    @EventHandler
    public void preLogin(final PreLoginEvent event) {
        if(mojangSessionServiceMonitor.getState() == SessionState.OFFLINE) {
            event.getConnection().setOnlineMode(false);
            log(event.getConnection(), "Starting offline login");
            doLogin(event, event.getConnection());
        }
    }

    @EventHandler
    public void login(final LoginEvent event) {
        if(!pendingConnections.containsKey(event.getConnection())) {
            log(event.getConnection(), "Starting online login");
            doLogin(event, event.getConnection());
        }
    }

    @EventHandler
    public void postLogin(final PostLoginEvent event) {
        final User user = pendingConnections.remove(event.getPlayer().getPendingConnection());
        if(user != null) {
            log(event.getPlayer().getPendingConnection(), "Completing login");
            userStore.addUser(event.getPlayer(), user);
            applyPermissions(event.getPlayer(), user);
            updateServerStatus(proxy.getOnlineCount());
        } else {
            logger.severe("No pending connection for " + event.getPlayer().getName() + ":" + event.getPlayer().getUniqueId());
            event.getPlayer().disconnect(INTERNAL_ERROR);
        }
    }

    private void doLogin(final AsyncEvent event, final PendingConnection connection) {
        event.registerIntent(this.plugin);

        final Server localServer = minecraftService.getLocalServer();

        // null if called from preLogin
        final UUID uuid = connection.getUniqueId();

        String username = null;
        if(uuid != null && localServer.fake_usernames() != null) {
            username = minecraftService.getLocalServer().fake_usernames().get(uuid);
        }
        if(username == null) {
            username = connection.getName();
        }

        final LoginRequest loginRequest = new LoginRequest(username,
                                                           uuid,
                                                           connection.getAddress().getAddress(),
                                                           minecraftService.getLocalServer(),
                                                           connection.getVirtualHost(),
                                                           false,
                                                           MinecraftVersion.describeProtocol(connection.getVersion()));

        Futures.addCallback(userService.login(loginRequest), new SystemFutureCallback<LoginResponse>() {
            @Override
            public void onSuccessThrows(LoginResponse response) {
                if(response.kick() != null) {
                    // NOTE: if login is cancelled, other fields in the response may be null
                    this.finish(true, response.message());
                    return;
                }

                final Map<String, Boolean> permissions = mergePermissions(response.user());
                if(!Boolean.TRUE.equals(permissions.get(Permissions.LOGIN))) {
                    this.finish(true, NOT_ALLOWED);
                    return;
                }

                // If we are doing an offline login, UUID will be null at this point,
                // and we need to set it to the correct one from the user document,
                // so that Bungee doesn't set it to a random one later.
                if(uuid == null) {
                    connection.setUniqueId(response.user().uuid());
                }

                pendingConnections.put(connection, response.user());

                if (response.route_to_server() != null) {
                    ServerInfo target = proxy.getServerInfo(response.route_to_server());
                    if (target == null) {
                        this.finish(true, "Routing to server failed\n\nPlease try again later");
                        return;
                    }
                    initialServers.put(connection, target);
                }

                this.finish(false, response.message());
            }

            @Override
            public void onFailure(Throwable throwable) {
                super.onFailure(throwable);
                this.finish(true, INTERNAL_ERROR);
            }

            private void finish(boolean cancelled, String message) {
                if(cancelled) {
                    log(connection, "Denying login: " + message);
                    cancelEvent(event, message);
                } else {
                    log(connection, "Allowing login");
                }
                event.completeIntent(LoginListener.this.plugin);
            }
        });
    }

    private Map<String, Boolean> mergePermissions(User user) {
        return Permissions.mergePermissions(minecraftService.getLocalServer().realms(), user.mc_permissions_by_realm());
    }

    private void applyPermissions(ProxiedPlayer player, User user) {
        for(Map.Entry<String, Boolean> entry : mergePermissions(user).entrySet()) {
            player.setPermission(entry.getKey(), entry.getValue());
        }
    }

    private void cancelEvent(Event event, String reason) {
        if(event instanceof Cancellable) {
            ((Cancellable) event).setCancelled(true);
        }
        if((event instanceof LoginEvent)) {
            ((LoginEvent) event).setCancelReason(reason);
        } else if(event instanceof PreLoginEvent) {
            ((PreLoginEvent) event).setCancelReason(reason);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void connect(ServerConnectEvent event) {
        ServerInfo target = initialServers.remove(event.getPlayer().getPendingConnection());
        if(target != null) {
            log(event.getPlayer().getPendingConnection(), "Routing to initial server " + target.getName());
            event.setTarget(target);
        }
    }

    @EventHandler
    public void disconnect(final PlayerDisconnectEvent event) {
        pendingConnections.remove(event.getPlayer().getPendingConnection());
        PlayerId playerId = userStore.removeUser(event.getPlayer());
        if(playerId != null) {
            log(event.getPlayer().getPendingConnection(), "Logging out");
            LogoutRequest logoutRequest = new LogoutRequest(playerId, this.minecraftService.getLocalServer());
            this.userService.logout(logoutRequest);
            updateServerStatus(proxy.getOnlineCount() - 1); // Adjust for disconnecting player included in count
        }
    }

    private void updateServerStatus(int count) {
        minecraftService.updateLocalServer(new ServerDoc.StatusUpdate() {
            @Override public int max_players() {
                return proxy.getConfig().getPlayerLimit();
            }

            @Override public int num_observing() {
                return count;
            }
        });
    }
}
