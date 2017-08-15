package tc.oc.commons.bukkit.users;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import me.anxuiz.settings.Setting;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import tc.oc.api.bukkit.friends.OnlineFriends;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.bukkit.users.OnlinePlayers;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.Session;
import tc.oc.api.docs.User;
import tc.oc.api.docs.UserId;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.message.MessageListener;
import tc.oc.api.message.MessageQueue;
import tc.oc.api.minecraft.MinecraftService;
import tc.oc.api.servers.ServerStore;
import tc.oc.api.sessions.SessionChange;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.commons.bukkit.chat.PlayerComponent;
import tc.oc.commons.bukkit.event.UserLoginEvent;
import tc.oc.commons.bukkit.format.ServerFormatter;
import tc.oc.commons.bukkit.nick.Identity;
import tc.oc.commons.bukkit.nick.IdentityProvider;
import tc.oc.commons.bukkit.settings.SettingManagerProvider;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.commons.core.util.Lazy;
import tc.oc.minecraft.scheduler.SyncExecutor;

import static com.google.common.base.Preconditions.checkArgument;
import static tc.oc.commons.core.IterableUtils.none;
import static tc.oc.commons.core.util.Nullables.first;
import static tc.oc.commons.core.util.Utils.notEqual;

/**
 * Receives {@link SessionChange} messages from the topic exchange and generates
 * connect/disconnect/change server announcements. ALL announcements are generated
 * from queue messages, even local ones. However, local announcements are synced
 * with their respective Bukkit events, to ensure that they appear ordered correctly
 * in chat, and names are rendered in the correct state.
 *
 * The {@link JoinMessageSetting} affects both local and remote announcements,
 * except that remote events are never displayed to non-friends.
 *
 * If join messages are disabled in the plugin config, no announcements will be
 * made at all by this service.
 *
 * When a player changes their nickname, it will appear exactly as if the old identity
 * disconnected from the network, and the new identity connected, to viewers who can't
 * see through their disguise. If the change is immediate (i.e. nick -i) then both
 * events will appear together in chat on the local server. Non-immediate changes will
 * only show one message in chat, since the other identity is on a different server.
 */
public class JoinMessageAnnouncer implements MessageListener, Listener, PluginFacet {

    private final MessageQueue queue;
    private final OnlineFriends onlineFriends;
    private final IdentityProvider identityProvider;
    private final SettingManagerProvider playerSettings;
    private final JoinMessageConfiguration config;
    private final Audiences audiences;
    private final ServerStore serverStore;
    private final MinecraftService minecraftService;
    private final OnlinePlayers onlinePlayers;
    private final SyncExecutor syncExecutor;
    private final BukkitUserStore userStore;
    private final Setting setting;

    // Events involving the local server are delayed until the actual Bukkit event,
    // so that the player's name is rendered in the correct state. These caches
    // hold the queue message during that delay.
    private final Cache<UserId, SessionChange> pendingJoins = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build();
    private final Cache<UserId, SessionChange> pendingQuits = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build();

    @Inject JoinMessageAnnouncer(MessageQueue queue,
                                 OnlineFriends onlineFriends,
                                 IdentityProvider identityProvider,
                                 SettingManagerProvider playerSettings,
                                 JoinMessageConfiguration config,
                                 Audiences audiences,
                                 ServerStore serverStore,
                                 MinecraftService minecraftService,
                                 OnlinePlayers onlinePlayers,
                                 SyncExecutor syncExecutor,
                                 BukkitUserStore userStore) {
        this.queue = queue;
        this.onlineFriends = onlineFriends;
        this.identityProvider = identityProvider;
        this.playerSettings = playerSettings;
        this.config = config;
        this.audiences = audiences;
        this.serverStore = serverStore;
        this.minecraftService = minecraftService;
        this.onlinePlayers = onlinePlayers;
        this.syncExecutor = syncExecutor;
        this.userStore = userStore;
        this.setting = JoinMessageSetting.get();
    }

    @Override
    public boolean isActive() {
        return config.enabled();
    }

    @Override
    public void enable() {
        queue.subscribe(this, syncExecutor);
        queue.bind(SessionChange.class);
    }

    @Override
    public void disable() {
        queue.unsubscribe(this);
    }

    @HandleMessage
    public void onSessionChange(SessionChange change) {
        checkArgument(change.old_session() != null || change.new_session() != null);

        final Server localServer = minecraftService.getLocalServer();
        final boolean localBefore = change.old_session() != null && change.old_session().server_id().equals(localServer._id());
        final boolean localAfter = change.new_session() != null && change.new_session().server_id().equals(localServer._id());

        if(!localBefore && localAfter && onlinePlayers.find(change.new_session().user()) == null) {
            // Joining player is not here yet
            pendingJoins.put(change.new_session().user(), change);
        } else if(localBefore && !localAfter && onlinePlayers.find(change.old_session().user()) != null) {
            // Quitting player hasn't left yet
            pendingQuits.put(change.old_session().user(), change);
        } else {
            announce(change);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void preJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onJoin(UserLoginEvent event) {
        final User user = userStore.getUser(event.getPlayer());
        final SessionChange change = pendingJoins.getIfPresent(user);
        if(change != null) {
            pendingJoins.invalidate(user);
            announce(change);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) throws EventException {
        event.setQuitMessage(null);
        final User user = userStore.getUser(event.getPlayer());
        final SessionChange change = pendingQuits.getIfPresent(user);

        event.yield();

        if(change != null) {
            pendingQuits.invalidate(user);
            announce(change);
        }
    }

    private void announce(SessionChange change) {
        final ChangedSession finished = new ChangedSession(change.old_session());
        final ChangedSession started = new ChangedSession(change.new_session());
        final PlayerId playerId = first(change.old_session(), change.new_session()).user();

        // If neither session is from the local server, just loop through
        // friends of the player, instead of all players online.
        final Stream<? extends Player> viewers =
            finished.isLocal() || started.isLocal() ? onlinePlayers.all().stream()
                                                    : onlineFriends.onlineFriends(playerId);

        // Use lazy messages so we can reuse them for multiple viewers,
        // without generating the ones we don't need. Depending on the
        // situation, we could end up showing one, two, or all three of
        // these messages for a single event.

        final Lazy<BaseComponent> leaveMessage = Lazy.from(() -> {
            final Component c = new Component(ChatColor.YELLOW);
            if(!minecraftService.isLocalServer(finished.server)) {
                c.extra(ServerFormatter.dark.nameWithDatacenter(finished.server)).extra(" ");
            }
            return c.extra(new TranslatableComponent("broadcast.leaveMessage", new PlayerComponent(finished.identity, NameStyle.VERBOSE)));
        });

        final Lazy<BaseComponent> joinMessage = Lazy.from(() -> {
            final Component c = new Component(ChatColor.YELLOW);
            if(!minecraftService.isLocalServer(started.server)) {
                c.extra(ServerFormatter.dark.nameWithDatacenter(started.server)).extra(" ");
            }
            return c.extra(new TranslatableComponent("broadcast.joinMessage", new PlayerComponent(started.identity, NameStyle.VERBOSE)));
        });

        final Lazy<BaseComponent> changeMessage = Lazy.from(() -> new Component(ChatColor.YELLOW)
            .extra(ServerFormatter.dark.nameWithDatacenter(finished.server))
            .extra(" \u00BB ")
            .extra(ServerFormatter.dark.nameWithDatacenter(started.server))
            .extra(" ")
            .extra(new TranslatableComponent("broadcast.changeServerMessage", new PlayerComponent(started.identity, NameStyle.VERBOSE))));

        viewers.forEach(viewerPlayer -> {
            if(viewerPlayer.getName().equals(playerId.username())) return;
            final Viewer viewer = new Viewer(viewerPlayer);

            if(!viewer.sendChangeServer(finished, started, changeMessage)) {
                viewer.sendJoinLeave(leaveMessage, finished, started);
                viewer.sendJoinLeave(joinMessage, started, finished);
            }
        });
    }

    class ChangedSession {
        final Identity identity;
        final Server server;

        private ChangedSession(@Nullable Session session) {
            identity = session == null ? null : identityProvider.createIdentity(session);
            server = session == null ? null : serverStore.byId(session.server_id());
        }

        boolean isLocal() {
            return server != null && minecraftService.isLocalServer(server);
        }

        boolean isVisible() {
            // Can't see a session that does not exist
            if(server == null) return false;

            // Local sessions are always visible
            if(minecraftService.isLocalServer(server)) return true;

            // Private server sessions are never visible
            if(server.visibility() == ServerDoc.Visibility.PRIVATE) return false;

            // Sessions from other networks are (configurably) invisible
            if(!(config.crossNetwork() || minecraftService.getLocalServer().network().equals(server.network()))) return false;

            // Check family/realm visibility filters
            if(!config.families().test(server.family())) return false;
            if(none(server.realms(), config.realms())) return false;

            return true;
        }

        boolean isVisibleTo(CommandSender viewer) {
            // Remote sessions are never visible to non-friends
            return isVisible() && (isLocal() || identity.isFriend(viewer));
        }

        boolean belongsTo(Identity identity, CommandSender viewer) {
            return isVisibleTo(viewer) && this.identity.isSamePerson(identity, viewer);
        }
    }

    class Viewer {
        final Player player;
        final Audience audience;
        final JoinMessageSetting.Options jms;

        private Viewer(Player player) {
            this.player = player;
            this.audience = audiences.get(player);
            this.jms = playerSettings.getManager(player).getValue(setting, JoinMessageSetting.Options.class);
        }

        boolean sendChangeServer(ChangedSession finished, ChangedSession started, Lazy<BaseComponent> message) {
            // If both sessions are visible,
            // and the sessions are on different servers,
            // and the sessions appear to belong to the same person,
            // then show a "change server" message.

            if(finished.isVisibleTo(player) && started.isVisibleTo(player) &&
               notEqual(finished.server, started.server) &&
               finished.identity.isSamePerson(started.identity, player)) {

                if(jms.isAllowed(started.identity.familiarity(player))) {
                    audience.sendMessage(message.get());
                }
                return true;
            }
            return false;
        }

        void sendJoinLeave(Lazy<BaseComponent> message, ChangedSession session, ChangedSession other) {
            // If session A is visible,
            // and session B does not appear to be another session belonging to the same person as session A,
            // and the viewer's setting allows messages about the owner of session A,
            // then inform the viewer about the start/finish of session A.

            if(session.isVisibleTo(player) &&
               !other.belongsTo(session.identity, player) &&
               jms.isAllowed(session.identity.familiarity(player))) {

                audience.sendMessage(message.get());
            }
        }
    }
}
