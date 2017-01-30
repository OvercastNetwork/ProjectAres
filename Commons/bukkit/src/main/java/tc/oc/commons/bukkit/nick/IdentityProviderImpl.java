package tc.oc.commons.bukkit.nick;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventBus;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import tc.oc.api.bukkit.friends.OnlineFriends;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.bukkit.users.OnlinePlayers;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.Session;
import tc.oc.api.docs.UserId;
import tc.oc.minecraft.scheduler.SyncExecutor;
import tc.oc.api.users.UserSearchResponse;
import tc.oc.commons.bukkit.event.UserLoginEvent;
import tc.oc.commons.bukkit.util.PlayerStates;
import tc.oc.commons.core.plugin.PluginFacet;

@Singleton
public class IdentityProviderImpl implements IdentityProvider, Listener, PluginFacet {

    private static final String REVEAL_ALL_PERMISSION = "nick.see-through-all";

    private final BukkitUserStore userStore;
    private final EventBus eventBus;
    private final OnlinePlayers onlinePlayers;
    private final PlayerStates playerStates;
    private final OnlineFriends friendMap;
    private final SyncExecutor syncExecutor;
    private final ConsoleIdentity consoleIdentity;

    private final Map<Player, Identity> identities = new HashMap<>();
    private final Map<String, Player> nicknames = new HashMap<>();

    @Inject
    IdentityProviderImpl(BukkitUserStore userStore, EventBus eventBus, OnlinePlayers onlinePlayers, PlayerStates playerStates, OnlineFriends friendMap, SyncExecutor syncExecutor, ConsoleIdentity consoleIdentity) {
        this.userStore = userStore;
        this.eventBus = eventBus;
        this.onlinePlayers = onlinePlayers;
        this.playerStates = playerStates;
        this.friendMap = friendMap;
        this.syncExecutor = syncExecutor;
        this.consoleIdentity = consoleIdentity;
    }

    @Override
    public boolean revealAll(CommandSender viewer) {
        return viewer.hasPermission(REVEAL_ALL_PERMISSION);
    }

    @Override
    public boolean reveal(CommandSender viewer, UserId userId) {
        return revealAll(viewer) || friendMap.areFriends(viewer, userId);
    }

    @Override
    public Identity createIdentity(PlayerId playerId, @Nullable String nickname) {
        return new IdentityImpl(onlinePlayers, friendMap, playerStates, this, playerId, nickname);
    }

    @Override
    public Identity createIdentity(Player player, @Nullable String nickname) {
        return createIdentity(userStore.getUser(player), nickname);
    }

    @Override
    public Identity createIdentity(CommandSender player) {
        return player instanceof Player ? createIdentity((Player) player, null) : consoleIdentity();
    }

    @Override
    public Identity consoleIdentity() {
        return consoleIdentity;
    }

    @Override
    public Identity createIdentity(Session session) {
        return createIdentity(session.user(), session.nickname());
    }

    @Override
    public Identity createIdentity(UserSearchResponse response) {
        if(response.last_session != null) {
            return createIdentity(response.user, response.last_session.nickname());
        } else {
            return createIdentity(response.user, null);
        }
    }

    @Override
    public Identity currentIdentity(CommandSender player) {
        if(player instanceof Player) {
            return currentIdentity(userStore.getUser((Player) player), (Player) player);
        } else {
            return consoleIdentity;
        }
    }

    @Override
    public Identity currentIdentity(PlayerId playerId) {
        return currentIdentity(playerId, onlinePlayers.find(playerId));
    }

    private Identity currentIdentity(PlayerId playerId, @Nullable Player player) {
        Identity identity = identities.get(player);
        if(identity == null) {
            identity = createIdentity(playerId, null);
            if(player != null && player.willBeOnline()) {
                identities.put(player, identity);
            }
        }
        return identity;
    }

    @Override
    public @Nullable Identity onlineIdentity(String name) {
        Player player = nicknames.get(name);
        if(player == null) player = onlinePlayers.find(name);
        return player == null ? null : currentIdentity(player);
    }

    @Override
    public void changeIdentity(Player player, @Nullable String nickname) {
        final Identity oldIdentity = currentIdentity(player);
        if(Objects.equals(oldIdentity.getNickname(), nickname)) return;

        applyNickname(player, oldIdentity.getNickname(), nickname);
        eventBus.callEvent(new PlayerIdentityChangeEvent(player, oldIdentity, currentIdentity(player)));
    }

    protected void validateNickname(@Nullable String nickname) {
        if(nickname != null && onlinePlayers.find(nickname) != null) {
            throw new IllegalArgumentException("Nickname '" + nickname + "' is the name of a real online player");
        }
    }

    protected void applyNickname(Player player, @Nullable String oldNickname, @Nullable String newNickname) {
        validateNickname(newNickname);

        if(oldNickname != null) {
            nicknames.remove(oldNickname);
        }

        final Identity identity = createIdentity(userStore.getUser(player), newNickname);
        if(player.willBeOnline()) {
            identities.put(player, identity);

            if(newNickname != null) {
                nicknames.put(newNickname, player);
            }
        }
    }

    /**
     * If a player has a nickname set in their user document on login, apply it.
     * This needs to run before {@link PlayerAppearanceListener#refreshNamesOnLogin}
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void applyNicknameOnLogin(UserLoginEvent event) {
        if(event.getUser().nickname() != null) {
            applyNickname(event.getPlayer(), null, event.getUser().nickname());
        }
    }

    /**
     * Clean up after quitting players
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void deactivateNickOnQuit(PlayerQuitEvent event) {
        final Identity identity = identities.remove(event.getPlayer());
        if(identity != null && identity.getNickname() != null) {
            nicknames.remove(identity.getNickname());
        }
    }

    /**
     * Clear any nickname that collides with the real name of a player logging in.
     * This ensures that usernames + nicknames together contain no duplicates.
     * The user who's nickname was cleared is not notified of this, but this
     * should be an extremely rare situation, so it's not a big problem.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void clearConflictingNicks(AsyncPlayerPreLoginEvent event) {
        final String name = event.getName();
        syncExecutor.execute(() -> {
            final Player player = nicknames.get(name);
            if(player != null) {
                changeIdentity(player, null);
            }
        });
    }
}
