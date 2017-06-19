package tc.oc.commons.bukkit.teleport;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.inject.Singleton;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.api.docs.Arena;
import tc.oc.api.docs.Game;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.games.ArenaStore;
import tc.oc.api.games.GameStore;
import tc.oc.api.model.ModelDispatcher;
import tc.oc.api.model.ModelListener;
import tc.oc.api.servers.ServerStore;
import tc.oc.commons.bukkit.format.GameFormatter;
import tc.oc.commons.bukkit.ticket.TicketBooth;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.commons.core.util.CacheUtils;
import tc.oc.commons.core.util.Utils;

@Singleton
public class Navigator implements PluginFacet, ModelListener, Commands {

    private static final char SERVER_SIGIL = '@';
    private static final char FAMILY_SIGIL = '.';
    private static final char GAME_SIGIL = '!';
    private static final char SPECIAL_SIGIL = '$';

    private final GameStore games;
    private final ArenaStore arenas;
    private final ServerStore servers;
    private final Teleporter teleporter;
    private final TicketBooth ticketBooth;
    private final FeaturedServerTracker featuredServerTracker;

    private final EmptyConnector emptyConnector = new EmptyConnector();
    private final DefaultConnector defaultConnector = new DefaultConnector();
    private final LoadingCache<String, SingleServerConnector> serverConnectors = CacheUtils.newCache(SingleServerConnector::new);
    private final LoadingCache<String, FeaturedServerConnector> familyConnectors = CacheUtils.newCache(FeaturedServerConnector::new);
    private final LoadingCache<String, GameConnector> gameConnectors = CacheUtils.newCache(GameConnector::new);

    @Inject Navigator(GameStore games, ArenaStore arenas, ServerStore servers, Teleporter teleporter, TicketBooth ticketBooth, ModelDispatcher modelDispatcher, FeaturedServerTracker featuredServerTracker) {
        this.games = games;
        this.arenas = arenas;
        this.servers = servers;
        this.teleporter = teleporter;
        this.ticketBooth = ticketBooth;
        this.featuredServerTracker = featuredServerTracker;
        modelDispatcher.subscribe(this);
    }

    @Command(
            aliases = { "showcachedconnectors" },
            desc = "Print a list of cached connectors",
            min = 0,
            max = 0
    )
    @CommandPermissions("ocn.developer")
    public void servers(final CommandContext args, final CommandSender sender) throws CommandException {
        sender.sendMessage("Cached Connectors:");
        final Map<String, SingleServerConnector> servers = serverConnectors.asMap();
        for (Map.Entry<String, SingleServerConnector> value : servers.entrySet()) {
            sender.sendMessage(value.getKey() + " : " + value.getValue().toString());
        }

        final Map<String, FeaturedServerConnector> families = familyConnectors.asMap();
        for (Map.Entry<String, FeaturedServerConnector> value :families.entrySet()) {
            sender.sendMessage(value.getKey() + " : " + value.getValue().toString());
        }

        final Map<String, GameConnector> games = gameConnectors.asMap();
        for (Map.Entry<String, GameConnector> value : games.entrySet()) {
            sender.sendMessage(value.getKey() + " : " + value.getValue().toString());
        }
    }

    private String localDatacenter() {
        return featuredServerTracker.localDatacenter();
    }

    public @Nullable Connector parseConnector(Collection<String> tokens) {
        final List<Connector> connectors = tokens.stream()
                                                 .map(this::parseConnector)
                                                 .filter(c -> c != null)
                                                 .collect(Collectors.toList());
        return connectors.isEmpty() ? null : new MultiConnector(connectors);
    }

    public @Nullable Connector parseConnector(String token) {
        if(token.length() == 0) return null;

        final String name = token.substring(1);
        switch(token.charAt(0)) {
            case SERVER_SIGIL: return serverConnectors.getUnchecked(name);
            case FAMILY_SIGIL: return familyConnectors.getUnchecked(name);
            case GAME_SIGIL: return gameConnectors.getUnchecked(name);
            case SPECIAL_SIGIL:
                switch(name) {
                    case "default": return defaultConnector;
                }
                break;
        }

        return null;
    }

    public Connector combineConnectors(List<Connector> connectors) {
        switch(connectors.size()) {
            case 0: return emptyConnector;
            case 1: return connectors.get(0);
            default: return new MultiConnector(connectors);
        }
    }

    @HandleModel
    public void serverUpdated(@Nullable Server before, @Nullable Server after, Server latest) {
        if(latest.bungee_name() != null) {
            final SingleServerConnector serverConnector = serverConnectors.getIfPresent(latest.bungee_name());
            if(serverConnector != null) serverConnector.refresh();
        }

        if(latest.family() != null) {
            final FeaturedServerConnector featuredServerConnector = familyConnectors.getIfPresent(latest.family());
            if(featuredServerConnector != null) featuredServerConnector.refresh();
        }
    }

    @HandleModel
    public void arenaUpdated(@Nullable Arena before, @Nullable Arena after, Arena latest) {
        final GameConnector gameConnector = gameConnectors.getIfPresent(latest.game_id());
        if(gameConnector != null) gameConnector.refresh();
    }

    @HandleModel
    public void gameUpdated(@Nullable Game before, @Nullable Game after, Game latest) {
        final GameConnector gameConnector = gameConnectors.getIfPresent(latest._id());
        if(gameConnector != null) gameConnector.refresh();
    }

    public static final Object DEFAULT_MAPPING = new Object();

    public abstract class Connector {
        public void startObserving(Consumer<Connector> observer) {}
        public void stopObserving(Consumer<Connector> observer) {}
        public void release() {}

        public abstract @Nullable Object mappedTo();
        public boolean isVisible() { return true; }
        public boolean isConnectable() { return true; }
        public int priority() { return 0; }
        public @Nullable BaseComponent description() { return null; }

        public abstract void teleport(Player player);
    }

    public class EmptyConnector extends Connector {
        @Override
        public Object mappedTo() { return null; }

        @Override
        public void teleport(Player player) {}
    }

    public class DefaultConnector extends Connector {
        @Override
        public String toString() {
            return getClass().getSimpleName() + "{}";
        }

        @Override
        public Object mappedTo() {
            return DEFAULT_MAPPING;
        }

        @Override
        public void teleport(Player player) {
            if(ticketBooth.currentGame(player) != null) {
                ticketBooth.leaveGame(player, true);
            } else {
                teleporter.sendToLobby(player, false);
            }
        }
    }

    public abstract class DynamicConnector extends Connector {
        private final Set<Consumer<Connector>> observers = new HashSet<>();

        @Override
        public void startObserving(Consumer<Connector> observer) {
            observers.add(observer);
        }

        @Override
        public void stopObserving(Consumer<Connector> observer) {
            observers.remove(observer);
        }

        protected void notifyObservers() {
            observers.forEach(o -> o.accept(this));
        }
    }

    public abstract class ServerConnector extends DynamicConnector {
        @Nullable protected Server server;

        @Override
        public void teleport(Player player) {
            teleporter.remoteTeleport(player, server);
        }

        @Override
        public Object mappedTo() {
            return server;
        }

        @Override
        public BaseComponent description() {
            return server != null && server.description() != null ? new TranslatableComponent(server.description())
                                                                  : super.description();
        }

        @Override
        public boolean isVisible() {
            return server != null &&
                   server.datacenter().equals(localDatacenter()) &&
                   server.visibility() == ServerDoc.Visibility.PUBLIC &&
                   server.online();
        }

        @Override
        public boolean isConnectable() {
            return server != null &&
                   server.datacenter().equals(localDatacenter()) &&
                   server.visibility() != ServerDoc.Visibility.PRIVATE &&
                   server.online();
        }
    }

    public class SingleServerConnector extends ServerConnector {
        private final String bungeeName;

        public SingleServerConnector(String bungeeName) {
            this.bungeeName = bungeeName;
            refresh();
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{server=" + bungeeName + "}";
        }

        protected void refresh() {
            server = servers.tryBungeeName(bungeeName);
            notifyObservers();
        }
    }

    public class FeaturedServerConnector extends ServerConnector {
        private final String familyId;

        public FeaturedServerConnector(String familyId) {
            this.familyId = familyId;
            refresh();
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{family=" + familyId + "}";
        }

        protected void refresh() {
            server = featuredServerTracker.featuredServerForFamily(familyId);
            notifyObservers();
        }
    }

    public class GameConnector extends DynamicConnector {
        private final String gameId;
        private @Nullable Game game;
        private @Nullable Arena arena;

        public GameConnector(String gameId) {
            this.gameId = gameId;
            refresh();
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{game=" + gameId + "}";
        }

        @Override
        public Object mappedTo() {
            return arena;
        }

        @Override
        public BaseComponent description() {
            return game != null ? new TranslatableComponent(GameFormatter.descriptionKey(game))
                                : super.description();
        }

        @Override
        public boolean isVisible() {
            return arena != null &&
                   game.visibility() == ServerDoc.Visibility.PUBLIC;
        }

        @Override
        public boolean isConnectable() {
            return arena != null &&
                   game.visibility() != ServerDoc.Visibility.PRIVATE;
        }

        public void refresh() {
            arena = arenas.tryDatacenterAndGameId(localDatacenter(), gameId);
            game = arena == null ? null : games.byId(arena.game_id());
            notifyObservers();
        }

        @Override
        public void teleport(Player player) {
            ticketBooth.playGame(player, arena);
        }
    }

    private class MultiConnector extends DynamicConnector {

        private final ImmutableList<Connector> connectors;
        private final Consumer<Connector> observer = this::refresh;
        private @Nullable Connector mapped;

        private MultiConnector(List<Connector> connectors) {
            this.connectors = ImmutableList.copyOf(connectors);
            for(Connector connector : this.connectors) {
                connector.startObserving(observer);
            }
            refresh(null);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() +
                   "{connectors=[" +
                   connectors.stream()
                             .map(Object::toString)
                             .collect(Collectors.joining(", ")) +
                   "]}";
        }

        @Override
        public int hashCode() {
            return Objects.hash(connectors);
        }

        @Override
        public boolean equals(Object obj) {
            return Utils.equals(MultiConnector.class, this, obj, that -> this.connectors.equals(that.connectors));
        }

        @Override
        public Object mappedTo() {
            return mapped == null ? null : mapped.mappedTo();
        }

        @Override
        public BaseComponent description() {
            return mapped == null ? null : mapped.description();
        }

        @Override
        public boolean isVisible() {
            return mapped != null && mapped.isVisible();
        }

        @Override
        public boolean isConnectable() {
            return mapped != null && mapped.isConnectable();
        }

        @Override
        public void release() {
            connectors.forEach(c -> c.stopObserving(observer));
        }

        @Override
        public void teleport(Player player) {
            if(mapped != null && mapped.isConnectable()) {
                mapped.teleport(player);
            }
        }

        private @Nullable Connector choose() {
            return connectors.stream()
                             .filter(Connector::isVisible)
                             .findFirst()
                             .orElse(null);
        }

        private void refresh(@Nullable Connector changed) {
            final Connector mapped = choose();
            if(!Objects.equals(this.mapped, mapped)) {
                this.mapped = mapped;
                notifyObservers();
            } else if(changed != null && changed.equals(this.mapped)) {
                notifyObservers();
            }
        }
    }
}
