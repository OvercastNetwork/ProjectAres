package tc.oc.commons.bukkit.ticket;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.docs.Arena;
import tc.oc.api.docs.Game;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.Ticket;
import tc.oc.api.docs.virtual.MatchDoc;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.games.ArenaStore;
import tc.oc.api.games.GameStore;
import tc.oc.api.games.TicketService;
import tc.oc.api.games.TicketStore;
import tc.oc.api.message.types.PlayGameRequest;
import tc.oc.api.message.types.Reply;
import tc.oc.minecraft.scheduler.SyncExecutor;
import tc.oc.api.servers.ServerStore;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.chat.WarningComponent;
import tc.oc.commons.bukkit.format.GameFormatter;
import tc.oc.commons.bukkit.teleport.PlayerServerChanger;
import tc.oc.commons.bukkit.util.PlayerStates;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.commands.CommandFutureCallback;

/**
 * User actions for querying, joining, and leaving {@link Game}s
 */
@Singleton
public class TicketBooth {

    private final SyncExecutor syncExecutor;
    private final BukkitUserStore userStore;
    private final PlayerStates playerStates;
    private final Audiences audiences;
    private final TicketService ticketService;
    private final GameFormatter gameFormatter;
    private final GameStore games;
    private final ArenaStore arenas;
    private final TicketStore tickets;
    private final ServerStore servers;
    private final PlayerServerChanger serverChanger;
    private final Server localServer;

    private @Nullable PlayHandler playHandler;

    @Inject TicketBooth(SyncExecutor syncExecutor,
                        BukkitUserStore userStore,
                        PlayerStates playerStates,
                        Audiences audiences,
                        TicketService ticketService,
                        GameFormatter gameFormatter,
                        GameStore games,
                        ArenaStore arenas,
                        TicketStore tickets,
                        ServerStore servers,
                        PlayerServerChanger serverChanger,
                        Server localServer) {

        this.syncExecutor = syncExecutor;
        this.userStore = userStore;
        this.playerStates = playerStates;
        this.audiences = audiences;
        this.ticketService = ticketService;
        this.gameFormatter = gameFormatter;
        this.games = games;
        this.arenas = arenas;
        this.tickets = tickets;
        this.servers = servers;
        this.serverChanger = serverChanger;
        this.localServer = localServer;
    }

    @FunctionalInterface
    public interface PlayHandler {
        boolean requestPlay(Player player);
    }

    public PlayHandler playHandler() {
        return playHandler;
    }

    public void setPlayHandler(PlayHandler handler) {
        playHandler = handler;
    }

    public void removePlayHandler(PlayHandler handler) {
        if(handler.equals(playHandler)) {
            playHandler = null;
        }
    }

    public Set<Game> allGames(CommandSender viewer) {
        return Sets.filter(games.set(), game -> game.visibility() == ServerDoc.Visibility.PUBLIC);
    }

    public void showGames(CommandSender sender) {
        gameFormatter.sendList(audiences.get(sender), allGames(sender));
    }

    public @Nullable Arena localArena() {
        final String id = localServer.arena_id();
        return id == null ? null : arenas.byId(id);
    }

    public @Nullable Game localGame() {
        final String id = localServer.game_id();
        return id == null ? null : games.byId(id);
    }

    public @Nullable Arena currentArena(Player player) {
        return currentArena(userStore.playerId(player));
    }

    public @Nullable Game currentGame(Player player) {
        return currentGame(userStore.playerId(player));
    }

    public @Nullable Arena currentArena(PlayerId playerId) {
        final Ticket ticket = tickets.tryUser(playerId);
        return ticket == null ? null : arenas.byId(ticket.arena_id());
    }

    public @Nullable Game currentGame(PlayerId playerId) {
        final Arena arena = currentArena(playerId);
        return arena == null ? null : games.byId(arena.game_id());
    }

    public @Nullable Game findGame(CommandSender sender, String name) {
        name = name.trim().toLowerCase();
        for(Game game : games.set()) {
            if(game.visibility() != ServerDoc.Visibility.PRIVATE &&
               name.equals(game.name().toLowerCase())) {
                return game;
            }
        }
        audiences.get(sender).sendMessage(new WarningComponent("game.unknown", name));
        showGames(sender);
        return null;
    }

    public @Nullable Arena findArena(CommandSender sender, @Nullable String name) {
        final Arena arena;
        if(name == null || name.length() == 0) {
            arena = localArena();
            if(arena == null) {
                showGames(sender);
            }
        } else {
            final Game game = findGame(sender, name);
            if(game == null) return null;

            arena = arenas.tryDatacenterAndGameId(localServer.datacenter(), game._id());
            if(arena == null) {
                audiences.get(sender).sendMessage(new WarningComponent("game.offline",  gameFormatter.name(game)));
            }
        }
        return arena;
    }

    private ListenableFuture<Reply> sendPlayRequest(Player player, @Nullable Arena arena) {
        return sendPlayRequest(userStore.playerId(player), arena);
    }

    private ListenableFuture<Reply> sendPlayRequest(PlayerId playerId, @Nullable Arena arena) {
        return sendPlayRequest(playerId, arena, false);
    }

    private ListenableFuture<Reply> sendPlayRequest(PlayerId playerId, @Nullable Arena arena, boolean force) {
        final Arena playing = currentArena(playerId);
        if(!force && Objects.equals(playing, arena)) {
            return Futures.immediateFuture(Reply.SUCCESS);
        } else {
            return ticketService.requestPlay(new PlayGameRequest() {
                @Override public String user_id() { return playerId._id(); }
                @Override public @Nullable String arena_id() { return arena == null ? null : arena._id(); }
            });
        }
    }

    public void leaveGame(Player player, boolean returnToLobby) {
        final Audience audience = audiences.get(player);

        final PlayerId playerId = userStore.playerId(player);
        final Game game = currentGame(playerId);
        if(game == null) {
            //audience.sendMessage(gameFormatter.notPlaying());
            return;
        }
        if(game != null) {
            syncExecutor.callback(
                sendPlayRequest(playerId, null),
                CommandFutureCallback.onSuccess(player, reply -> {
                    audience.sendMessage(gameFormatter.left(game));
                    if(returnToLobby) {
                        serverChanger.sendPlayerToLobby(player, true);
                    }
                })
            );
        }
    }

    public void playLocalGame(Player player) {
        final Game game = localGame();
        if(game != null) {
            final Arena arena = arenas.tryDatacenterAndGameId(localServer.datacenter(), game._id());
            if(arena != null) {
                playGame(player, arena);
            }
        } else if(playHandler != null) {
            playHandler.requestPlay(player);
        } else {
            showGames(player);
        }
    }

    public void playGame(Player player, @Nullable String name) {
        final Arena arena = findArena(player, name);
        if(arena != null) {
            playGame(player, arena);
        }
    }

    public void playGame(Player player, Arena arena) {
        final PlayerId playerId = userStore.playerId(player);
        boolean forceRequest = false;
        if(arena.equals(currentArena(playerId))) {
            final Game game = games.byId(arena.game_id());
            final Audience audience = audiences.get(player);
            final MatchDoc match = localServer.current_match();
            if(match != null && match.join_mid_match()) {
                audience.sendMessage(gameFormatter.alreadyPlaying(game));
                return;
            } else {
                audience.sendMessage(gameFormatter.replay(game));
                forceRequest = playerStates.isObserving(player);
            }
        }
        sendPlayRequest(playerId, arena, forceRequest);
    }

    public void watchGame(Player player, @Nullable String name) {
        final Arena arena = findArena(player, name);
        if(arena != null) {
            watchGame(player, arena);
        }
    }

    public void watchGame(Player player, Arena arena) {
        final Audience audience = audiences.get(player);
        final Game game = games.byId(arena.game_id());
        final Optional<Server> fullest = servers.byArena(arena)
                                                .stream()
                                                .filter(Server::online)
                                                .max(Comparator.comparing(Server::num_participating));
        if(!fullest.isPresent()) {
            audience.sendWarning(new TranslatableComponent("game.offline", gameFormatter.name(game)), false);
            return;
        } else if(fullest.get().num_participating() < 2) {
            audience.sendWarning(new TranslatableComponent("game.empty", gameFormatter.name(game)), false);
            return;
        }

        syncExecutor.callback(
            sendPlayRequest(player, null),
            reply -> {
                serverChanger.sendPlayerToServer(player, fullest.get(), false);
            }
        );
    }
}
