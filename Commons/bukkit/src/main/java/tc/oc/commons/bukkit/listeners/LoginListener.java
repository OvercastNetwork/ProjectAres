package tc.oc.commons.bukkit.listeners;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.bukkit.entity.Player;
import org.bukkit.event.EventBus;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.virtual.UserDoc;
import tc.oc.api.minecraft.MinecraftService;
import tc.oc.api.users.LoginRequest;
import tc.oc.api.users.LoginResponse;
import tc.oc.api.users.UserService;
import tc.oc.api.util.Permissions;
import tc.oc.commons.bukkit.chat.ComponentRenderContext;
import tc.oc.commons.bukkit.event.AsyncUserLoginEvent;
import tc.oc.commons.bukkit.event.UserLoginEvent;
import tc.oc.commons.bukkit.punishment.PunishmentFormatter;
import tc.oc.commons.bukkit.util.PermissionUtils;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.concurrent.Locker;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.minecraft.api.scheduler.Scheduler;
import tc.oc.minecraft.protocol.MinecraftVersion;

@Singleton
public class LoginListener implements Listener, PluginFacet {

    private static final String INTERNAL_SERVER_ERROR = "Sorry, but there was an internal server error.\n" +
                                                        "We are working to resolve the issue: please check back soon.";
    private static final String SERVER_IS_RESTARTING = "Server is restarting, please reconnect in a moment";

    private final Logger logger;
    private final Plugin plugin;
    private final EventBus eventBus;
    private final Scheduler scheduler;
    private final MinecraftService minecraftService;
    private final UserService userService;
    private final BukkitUserStore userStore;
    private final ComponentRenderContext renderer;
    private final PunishmentFormatter punishmentFormatter;

    private boolean connected;
    private final ReadWriteLock connectedLock = new ReentrantReadWriteLock();

    // MC login times out in 30 seconds so caching for 1 minute should be fine
    private final Cache<UUID, LoginResponse> logins = CacheBuilder.newBuilder()
                                                                  .expireAfterWrite(1, TimeUnit.MINUTES)
                                                                  .build();

    @Inject LoginListener(Loggers loggers, Plugin plugin, EventBus eventBus, Scheduler scheduler, MinecraftService minecraftService, UserService userService, BukkitUserStore userStore, ComponentRenderContext renderer, PunishmentFormatter punishmentFormatter) {
        this.eventBus = eventBus;
        this.logger = loggers.get(getClass());
        this.scheduler = scheduler;
        this.minecraftService = minecraftService;
        this.userService = userService;
        this.userStore = userStore;
        this.plugin = plugin;
        this.renderer = renderer;
        this.punishmentFormatter = punishmentFormatter;
    }

    @Override
    public void enable() {
        try(Locker _ = Locker.lock(connectedLock.writeLock())) {
            connected = true;
        }
    }

    @Override
    public void disable()  {
        try(Locker _ = Locker.lock(connectedLock.writeLock())) {
            connected = false;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void preLogin(final AsyncPlayerPreLoginEvent event) {
        this.logger.info(event.getName() + " pre-login: uuid=" + event.getUniqueId() + " ip=" + event.getAddress());

        try(Locker _ = Locker.lock(connectedLock.readLock())) {
            this.logins.invalidate(event.getUniqueId());

            if(!connected) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, SERVER_IS_RESTARTING);
                return;
            }

            LoginResponse response = this.userService.login(
                new LoginRequest(event.getName(),
                                 event.getUniqueId(),
                                 event.getAddress(),
                                 minecraftService.getLocalServer(),
                                 true)
            ).get();

            if(response.kick() != null) switch(response.kick()) {
                case "error":
                    this.logger.info(event.getName() + " login error: " + response.message());
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, response.message());
                    break;

                case "banned": // Only used for IP bans right now
                    this.logger.info(event.getName() + " is banned");
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, response.message());
                    break;
            }

            if(event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;

            this.logins.put(event.getUniqueId(), response);

            eventBus.callEvent(new AsyncUserLoginEvent(response));
        } catch(Exception e) {
            this.logger.log(Level.SEVERE, e.toString(), e);
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, INTERNAL_SERVER_ERROR);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void login(PlayerLoginEvent event) {
        try {
            final Player player = event.getPlayer();
            final UUID uuid = player.getUniqueId();

            player.setGravity(true);

            this.logins.cleanUp();
            final LoginResponse response = this.logins.getIfPresent(uuid);
            this.logins.invalidate(uuid);

            if(response == null) {
                this.logger.warning("No login info for " + player.getName() + " " + uuid);
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, INTERNAL_SERVER_ERROR);
                return;
            }

            // TODO: Consider creating a PreUserLoginEvent that can be cancelled,
            // before things like sessions are started.

            userStore.addUser(player, response.user());

            applyPermissions(player, response.user());

            if(response.punishment() != null) {
                rejectLogin(event, punishmentFormatter.screen(response.punishment()));
            }

            if(!player.hasPermission(Permissions.LOGIN)) {
                rejectLogin(event, new TranslatableComponent("servers.notAllowed"));
            }

            if(event.getResult() == PlayerLoginEvent.Result.KICK_FULL) {
                // Allow privileged players to join when the server is full
                if(player.hasPermission("pgm.fullserver")) {
                    event.allow();
                } else {
                    rejectLogin(event, new TranslatableComponent("serverFull"));
                }
            }

            if(response.user().mc_locale() != null) {
                // If we have a saved locale for the player, apply it.
                // This should ensure that text displayed on join is properly
                // localized, as long as the player has connected once before.
                player.setLocale(response.user().mc_locale());
            }

            userService.update(response.user(), new UserDoc.ClientDetails() {
                @Override public String mc_client_version() {
                    return MinecraftVersion.describeProtocol(player.getProtocolVersion());
                }

                @Override public String skin_blob() {
                    return player.getSkin().getData();
                }
            });

            if(event.getResult() == PlayerLoginEvent.Result.KICK_OTHER) return;

            final UserLoginEvent ourEvent = new UserLoginEvent(
                player, response, event.getResult(),
                event.getKickMessage() == null || "".equals(event.getKickMessage()) ? null : new Component(event.getKickMessage())
            );

            eventBus.callEvent(ourEvent);

            event.setResult(ourEvent.getResult());
            event.setKickMessage(ourEvent.getKickMessage() == null ? "" : renderer.renderLegacy(ourEvent.getKickMessage(), player));
        }
        catch(Exception e) {
            this.logger.log(Level.SEVERE, e.toString(), e);
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, INTERNAL_SERVER_ERROR);
        }
    }

    protected void applyPermissions(Player player, UserDoc.Login userDoc) {
        boolean op = false;

        final Server localServer = minecraftService.getLocalServer();
        if(localServer.operators().containsKey(player.getUniqueId())) {
            logger.info("Opping " + player.getName() + " because they are in the server op list");
            op = true;
        }

        if(localServer.team() != null && localServer.team().members().contains(userDoc)) {
            logger.info("Opping " + player.getName() + " because they are on the team that owns the server");
            op = true;
        }

        PermissionAttachment attachment = player.addAttachment(this.plugin);
        PermissionUtils.setPermissions(attachment, Permissions.mergePermissions(localServer.realms(), userDoc.mc_permissions_by_realm()));
        player.recalculatePermissions();

        if(player.hasPermission("op")) {
            op = true;
            logger.info("Opping " + player.getName() + " because they have the op permission node");
        }

        player.setOp(op); // This is always explicitly set to true or false on login
    }

    protected void rejectLogin(PlayerLoginEvent event, BaseComponent message) {
        if(event.getResult() != PlayerLoginEvent.Result.KICK_OTHER) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, renderer.renderLegacy(message, event.getPlayer()));
        }
    }

    @EventHandler
    private void quit(PlayerQuitEvent event) {
        scheduler.runSync(() -> userStore.removeUser(event.getPlayer()));
    }
}
