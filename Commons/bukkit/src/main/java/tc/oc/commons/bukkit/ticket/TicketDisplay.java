package tc.oc.commons.bukkit.ticket;

import com.google.common.cache.LoadingCache;
import java.time.Duration;
import java.util.Collections;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.bukkit.users.OnlinePlayers;
import tc.oc.api.docs.Arena;
import tc.oc.api.docs.Game;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.Ticket;
import tc.oc.api.docs.virtual.MatchDoc;
import tc.oc.api.games.ArenaStore;
import tc.oc.api.games.GameStore;
import tc.oc.api.games.TicketStore;
import tc.oc.api.model.ModelDispatcher;
import tc.oc.api.model.ModelListener;
import tc.oc.api.servers.ServerStore;
import tc.oc.commons.bukkit.bossbar.BossBarFactory;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.format.GameFormatter;
import tc.oc.commons.bukkit.util.PlayerStates;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.commons.core.util.CacheUtils;
import tc.oc.minecraft.api.scheduler.Tickable;

/**
 * Displays the current state of {@link Game} queues to the {@link Player}s in each queue.
 */
@Singleton
public class TicketDisplay implements ModelListener, Listener, PluginFacet, Tickable {

    private final BukkitUserStore userStore;
    private final OnlinePlayers players;
    private final PlayerStates playerStates;
    private final Audiences audiences;
    private final GameFormatter gameFormatter;
    private final Server localServer;
    private final ServerStore servers;
    private final TicketStore tickets;
    private final ArenaStore arenas;
    private final GameStore games;

    private final LoadingCache<Arena, BossBar> bars;

    @Inject TicketDisplay(BukkitUserStore userStore, OnlinePlayers players, PlayerStates playerStates, Audiences audiences, GameFormatter gameFormatter, Server localServer, BossBarFactory bossBarFactory, ServerStore servers, TicketStore tickets, ArenaStore arenas, GameStore games, ModelDispatcher modelDispatcher) {
        this.userStore = userStore;
        this.players = players;
        this.playerStates = playerStates;
        this.audiences = audiences;
        this.gameFormatter = gameFormatter;
        this.localServer = localServer;
        this.servers = servers;
        this.tickets = tickets;
        this.arenas = arenas;
        this.games = games;
        this.bars = CacheUtils.newCache(game -> bossBarFactory.createRenderedBossBar());
        modelDispatcher.subscribe(this);
    }

    private void updateArena(Arena arena) {
        final Game game = games.byId(arena.game_id());
        int minPlayers = 0;
        if(arena.next_server_id() != null) {
            minPlayers = servers.byId(arena.next_server_id()).min_players();
        }
        final BaseComponent text;
        final double progress;
        if(minPlayers > 0 && arena.num_queued() < minPlayers) {
            text = gameFormatter.queued(game, minPlayers - arena.num_queued());
            progress = (double) arena.num_queued() / (double) minPlayers;
        } else {
            text = gameFormatter.joining(game);
            progress = 1;
        }
        bars.getUnchecked(arena).update(text, progress, BarColor.YELLOW, BarStyle.SOLID, Collections.emptySet());
    }

    @HandleModel
    public void arenaUpdated(@Nullable Arena before, @Nullable Arena after, Arena latest) {
        updateArena(latest);
    }

    @HandleModel
    public void ticketUpdated(@Nullable Ticket before, @Nullable Ticket after, Ticket latest) {
        final Arena arena = arenas.byId(latest.arena_id());
        updateArena(arena);

        final Player player = userStore.find(latest.user());
        if(player != null) {
            final BossBar bar = bars.getUnchecked(arena);
            if(after != null && after.server_id() == null) {
                bar.addPlayer(player);
            } else {
                bar.removePlayer(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        final Ticket ticket = tickets.tryUser(userStore.getUser(event.getPlayer()));
        if(ticket != null && ticket.server_id() == null) {
            bars.getUnchecked(arenas.byId(ticket.arena_id())).addPlayer(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        final Ticket ticket = tickets.tryUser(userStore.getUser(event.getPlayer()));
        if(ticket != null && ticket.server_id() == null) {
            bars.getUnchecked(arenas.byId(ticket.arena_id())).removePlayer(event.getPlayer());
        }
    }

    @Override
    public Duration tickPeriod() {
        return Duration.ofSeconds(1);
    }

    @Override
    public void tick() {
        final MatchDoc match = localServer.current_match();
        if(match != null && match.start() != null && !match.join_mid_match() && match.end() == null) {
            players.stream()
                   .filter(playerStates::isObserving)
                   .forEach(player -> {
                       final Ticket ticket = tickets.tryUser(userStore.tryUser(player));
                       if(ticket != null) {
                           final Game game = games.byId(arenas.byId(ticket.arena_id()).game_id());
                           if(game != null) {
                               audiences.get(player).sendHotbarMessage(gameFormatter.replayMaybe(game));
                           }
                       }
                   });
        }
    }

}
