package tc.oc.pgm.cycle;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import tc.oc.api.bukkit.users.OnlinePlayers;
import tc.oc.api.docs.Game;
import tc.oc.api.docs.Server;
import tc.oc.api.games.GameStore;
import tc.oc.api.games.TicketService;
import tc.oc.api.message.types.CycleRequest;
import tc.oc.api.servers.ServerStore;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.format.GameFormatter;
import tc.oc.commons.bukkit.teleport.PlayerServerChanger;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.BlankComponent;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.commands.CommandBinder;
import tc.oc.commons.core.util.Comparables;
import tc.oc.pgm.countdowns.MatchCountdown;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.MatchEndEvent;
import tc.oc.pgm.events.PlayerPartyChangeEvent;
import tc.oc.pgm.map.PGMMap;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchExecutor;
import tc.oc.pgm.match.MatchManager;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.Repeatable;
import tc.oc.pgm.match.inject.MatchModuleFixtureManifest;
import tc.oc.pgm.module.ModuleDescription;
import tc.oc.pgm.restart.RestartListener;
import tc.oc.time.Time;

@ModuleDescription(name = "Cycle")
@ListenerScope(MatchScope.LOADED)
public class CycleMatchModule extends MatchModule implements Listener {

    public static class Manifest extends MatchModuleFixtureManifest<CycleMatchModule> {
        @Override protected void configure() {
            super.configure();
            new CommandBinder(binder())
                .register(CycleCommands.class);
        }
    }

    @Inject private MatchManager mm;
    @Inject private RestartListener restartListener;
    @Inject private TicketService ticketService;
    @Inject private Server localServer;
    @Inject private PlayerServerChanger serverChanger;
    @Inject private CycleConfig config;
    @Inject private OnlinePlayers onlinePlayers;
    @Inject private GameStore games;
    @Inject private ServerStore servers;
    @Inject private Audiences audiences;
    @Inject private GameFormatter gameFormatter;
    @Inject private MatchExecutor matchExecutor;

    private boolean autoCycle = true;

    public CycleConfig getConfig() {
        return config;
    }

    public boolean isCycling() {
        return match.countdowns().getCountdown() instanceof CycleCountdown;
    }

    public void cycleNow() {
        cycleNow(null);
    }

    public void cycleNow(PGMMap map) {
        startCountdown(Duration.ZERO, map);
    }

    public void startCountdown(@Nullable Duration duration) {
        if(duration == null) duration = config.countdown();
        match.ensureNotRunning();
        if(Duration.ZERO.equals(duration)) {
            requestCycle(false);
        } else {
            getMatch().countdowns().start(new CycleCountdown(mm, getMatch()), duration);
        }
    }

    public void startCountdown(@Nullable Duration duration, PGMMap nextMap) {
        mm.setNextMap(nextMap);
        startCountdown(duration);
    }

    private void requestCycle(boolean retryRotation) {
        if(localServer.game_id() != null) {
            final PGMMap nextMap = mm.getNextMap();
            matchExecutor.callback(
                ticketService.requestCycle(
                    new CycleRequest() {
                        @Override public String server_id() {
                            return localServer._id();
                        }

                        @Override public String map_id() {
                            return nextMap.getDocument()._id();
                        }

                        @Override public int min_players() {
                            return nextMap.getPersistentContext().playerLimits().lowerEndpoint();
                        }

                        @Override public int max_players() {
                            return nextMap.getPersistentContext().playerLimits().upperEndpoint();
                        }
                    }
                ),
                (match0, reply) -> {
                    final List<ListenableFuture<?>> quitFutures = new ArrayList<>();

                    if(localServer.game_id() != null) {
                        for(Map.Entry<UUID, String> entry : reply.destinations().entrySet()) {
                            final String serverId = entry.getValue();
                            if(localServer._id().equals(serverId)) continue;

                            final Player player = onlinePlayers.find(entry.getKey());
                            if(player == null) continue;;

                            final Audience audience = audiences.get(player);
                            final Game game = games.byId(localServer.game_id());

                            if(serverId == null) {
                                quitFutures.add(serverChanger.sendPlayerToLobby(player, true));
                            } else {
                                audience.sendMessage(gameFormatter.rejoining(game));
                                quitFutures.add(serverChanger.sendPlayerToServer(player, servers.byId(serverId), true));
                            }
                        }
                    }

                    if(quitFutures.isEmpty()) {
                        doCycle(retryRotation);
                    } else {
                        // Cycle after all requeued players are gone
                        matchExecutor.callback(
                            Futures.allAsList(quitFutures),
                            (match1, list) -> doCycle(retryRotation)
                        );
                    }
                }
            );
        } else {
            doCycle(retryRotation);
        }
    }

    private void doCycle(boolean retryRotation) {
        mm.cycleToNext(getMatch(), retryRotation, false);
    }

    @EventHandler
    public void onPartyChange(PlayerPartyChangeEvent event) {
        if(match.isRunning() && match.getParticipatingPlayers().isEmpty()) {
            checkEmptyServerCycle();
        }
    }

    @EventHandler
    public void onMatchEnd(MatchEndEvent event) {
        checkMatchEndCycle();
    }

    @Repeatable(scope = MatchScope.LOADED, interval = @Time(seconds = 1))
    public void periodicCheck() {
        // Check auto-cycle conditions periodically, so we don't get stuck when something fails
        checkMatchEndCycle();
        checkEmptyServerCycle();
    }

    private boolean canAutoCycle() {
        return autoCycle &&
               !isCycling() &&
               !restartListener.willRestart(match);
    }

    private void checkMatchEndCycle() {
        if(canAutoCycle() && match.isFinished()) {
            final CycleConfig.Auto autoConfig = config.matchEnd();
            if(autoConfig.enabled()) {
                startCountdown(autoConfig.countdown());
            }
        }
    }

    private void checkEmptyServerCycle() {
        if(canAutoCycle() && match.isRunning() && match.getParticipatingPlayers().isEmpty()) {
            final CycleConfig.Auto autoConfig = config.matchEmpty();
            if(autoConfig.enabled()) {
                logger.info("Cycling due to empty match");
                startCountdown(autoConfig.countdown());
            }
        }
    }

    public class CycleCountdown extends MatchCountdown {
        protected final MatchManager mm;

        public CycleCountdown(MatchManager mm, Match match) {
            super(match);
            this.mm = mm;
        }

        @Override
        public BaseComponent barText(Player viewer) {
            PGMMap nextMap = mm.getNextMap();
            if(nextMap == null || !nextMap.isLoaded()) return BlankComponent.INSTANCE;
            BaseComponent mapName = new Component(nextMap.getInfo().name, ChatColor.AQUA);

            if(Comparables.greaterThan(remaining, Duration.ZERO)) {
                return new Component(new TranslatableComponent("countdown.cycle.message", mapName, secondsRemaining(ChatColor.DARK_RED)), ChatColor.DARK_AQUA);
            } else {
                return new Component(new TranslatableComponent("countdown.cycle.complete", mapName), ChatColor.DARK_AQUA);
            }
        }

        @Override
        public void onCancel(Duration remaining, Duration total, boolean manual) {
            super.onCancel(remaining, total, manual);
            if(manual && match.isFinished()) {
                CycleMatchModule.this.autoCycle = false;
            }
        }

        @Override
        public void onEnd(Duration total) {
            super.onEnd(total);
            requestCycle(true);
        }
    }
}
