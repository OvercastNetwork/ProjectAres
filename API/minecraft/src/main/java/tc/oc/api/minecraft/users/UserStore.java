package tc.oc.api.minecraft.users;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.Session;
import tc.oc.api.docs.SimplePlayerId;
import tc.oc.api.docs.User;
import tc.oc.api.docs.UserId;
import tc.oc.api.model.ModelSync;
import tc.oc.commons.core.util.ProxyUtils;
import tc.oc.minecraft.api.entity.Player;
import tc.oc.minecraft.api.server.LocalServer;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class UserStore<P extends Player> implements OnlinePlayers<P> {

    @Inject private LocalServer server;
    @Inject private @ModelSync Executor modelSync;

    private final Map<Player, User> users = new WeakHashMap<>();
    private final Map<Player, Session> sessions = new WeakHashMap<>();

    // Keys of this map are the actual proxies
    private final WeakHashMap<UserId, EverfreshUser> proxies = new WeakHashMap<>();

    private static class EverfreshUser implements Provider<User> {
        User real;
        WeakReference<User> proxy;

        @Override
        public User get() {
            return real;
        }
    }

    @Override
    public Collection<P> all() {
        return (Collection<P>) server.getOnlinePlayers();
    }

    @Override
    public @Nullable P find(String name) {
        return (P) server.getPlayerExact(name);
    }

    @Override
    public @Nullable P find(UUID uuid) {
        return (P) server.getPlayer(uuid);
    }

    @Override
    public @Nullable P find(UserId userId) {
        if(userId instanceof PlayerId) {
            return find(((PlayerId) userId).username());
        } else {
            final User user = tryUser(userId);
            return user == null ? null : find(user.username());
        }
    }

    protected void updateUser(Player player, @Nullable User before, @Nullable User after) {
        if(after == null) {
            users.remove(player);
            proxies.remove(before);
        } else {
            users.put(player, after);

            final EverfreshUser proxy = proxies.get(after);
            if(proxy != null) {
                proxy.real = after;
            }
        }
    }

    public void addUser(Player player, User user) {
        checkNotNull(user);
        updateUser(player, null, user);
    }

    public @Nullable User replaceUser(Player player, User user) {
        checkNotNull(player);
        checkNotNull(user);
        final User old = users.get(player);
        if(old != null) {
            updateUser(player, old, user);
        }
        return old;
    }

    public void handleUpdate(User replacement) {
        modelSync.execute(() -> {
            final P player = find(replacement.uuid());
            if(player != null) {
                replaceUser(player, replacement);
            }
        });
    }

    public @Nullable User removeUser(Player player) {
        final User old = users.get(player);
        if(old != null) {
            updateUser(player, old, null);
        }
        return old;
    }

    public boolean hasUser(Player player) {
        return users.containsKey(player);
    }

    public Optional<User> user(UserId userId) {
        return Optional.ofNullable(tryUser(userId));
    }

    public Optional<User> user(Player player) {
        return Optional.ofNullable(tryUser(player));
    }

    public @Nullable User tryUser(UserId userId) {
        // If the argument is a User already, return it
        if(userId instanceof User) {
            return (User) userId;
        }

        // Search the store for a full User
        for(User user : users.values()) {
            // User extends UserId, so they will compare equal if they are the same user
            if(userId.equals(user)) return user;
        }

        return null;
    }

    public @Nullable User tryUser(Player player) {
        return users.get(player);
    }

    public User getUser(UserId userId) {
        final User user = tryUser(userId);
        if(user == null) {
            throw new IllegalStateException("User " + userId + " has no cached user document");
        }
        return user;
    }

    public User getUser(Player player) {
        final User doc = tryUser(player);
        if(doc == null) {
            throw new IllegalStateException("Player " + player + " has no cached user document");
        }
        return doc;
    }

    public PlayerId playerId(UserId userId) {
        if(userId instanceof PlayerId) {
            return SimplePlayerId.copyOf((PlayerId) userId);
        }
        return SimplePlayerId.copyOf(getUser(userId));
    }

    /**
     * Return a light-weight {@link PlayerId} for the given player.
     */
    public PlayerId playerId(Player player) {
        return SimplePlayerId.copyOf(getUser(player));
    }

    /**
     * Return a dynamic {@link User} instance that stays in sync with the latest
     * version of the document.
     *
     * The given player must be online when this method is called, but the returned
     * document can be used even after they have disconnected, it just won't update.
     * If they reconnect, the old document will resume updating.
     *
     * These proxies are stored in a weak collection, so don't hold references to
     * them for any longer than necessary.
     */
    public User getEverfreshUser(Player player) {
        return getEverfreshUser(getUser(player));
    }

    public User getEverfreshUser(UserId user) {
        // Look for an existing proxy
        EverfreshUser provider = proxies.get(user);
        if(provider != null) {
            final User fake = provider.proxy.get();
            if(fake != null) {
                // If the proxy is still available, return it
                return fake;
            } else {
                // If the proxy was garbage collected, remove
                // the entry from the map, and create a new one.
                proxies.remove(user);
            }
        }

        // Create a new provider and store the latest User document in it.
        // Don't store the document passed to the method, because we don't
        // know how old it is, or whether or not it is a proxy itself.
        provider = new EverfreshUser();
        provider.real = getUser(get(user));

        // Store the proxy User in the provider, so that we can look it up later.
        // There is no fast way to get a key from a map, so we have to store
        // a copy of it in the value. It has to be a weak reference or the entry
        // in the weak map would never be released.
        final User proxy = ProxyUtils.newProviderProxy(User.class, provider);
        provider.proxy = new WeakReference<>(proxy);

        // Store the provider with the proxy User as the (weak) key
        proxies.put(proxy, provider);
        return proxy;
    }

    public void setSession(Player player, Session session) {
        sessions.put(player, checkNotNull(session));
    }

    public @Nullable Session removeSession(Player player) {
        return sessions.remove(player);
    }

    public Optional<Session> session(Player player) {
        return Optional.ofNullable(sessions.get(player));
    }

    public Session getSession(Player player) {
        Session session = sessions.get(player);
        if(session == null) {
            throw new IllegalStateException("Player " + player.getName() + " has no session");
        }
        return session;
    }
}
