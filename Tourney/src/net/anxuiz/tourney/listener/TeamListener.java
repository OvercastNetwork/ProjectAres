package net.anxuiz.tourney.listener;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import net.anxuiz.tourney.MatchManager;
import net.anxuiz.tourney.TeamManager;
import net.anxuiz.tourney.Tourney;
import net.anxuiz.tourney.TourneyState;
import net.anxuiz.tourney.event.EntrantRegisterEvent;
import net.anxuiz.tourney.event.EntrantUnregisterEvent;
import net.anxuiz.tourney.event.TourneyStateChangeEvent;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.bukkit.users.OnlinePlayers;
import tc.oc.api.docs.Entrant;
import tc.oc.api.docs.Tournament;
import tc.oc.commons.bukkit.event.UserLoginEvent;
import tc.oc.commons.core.chat.Component;
import tc.oc.pgm.blitz.BlitzMatchModuleImpl;
import tc.oc.pgm.channels.ChannelMatchModule;
import tc.oc.pgm.events.MatchEndEvent;
import tc.oc.pgm.events.MatchPlayerAddEvent;
import tc.oc.pgm.blitz.BlitzMatchModule;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchState;
import tc.oc.pgm.score.ScoreMatchModule;
import tc.oc.pgm.teams.Team;
import tc.oc.pgm.teams.TeamMatchModule;

@Singleton
public class TeamListener implements Listener {

    private final Tourney tourney;
    private final Tournament tournament;
    private final Provider<MatchManager> matchManagerProvider;
    private final Provider<TeamManager> teamManagerProvider;
    private final Provider<Match> matchProvider;
    private final OnlinePlayers onlinePlayers;
    private final BukkitUserStore userStore;

    @Inject private TeamListener(Tourney tourney, Tournament tournament, OnlinePlayers onlinePlayers, BukkitUserStore userStore, Provider<MatchManager> matchManagerProvider, Provider<TeamManager> teamManagerProvider, Provider<Match> matchProvider) {
        this.tourney = tourney;
        this.tournament = tournament;
        this.onlinePlayers = onlinePlayers;
        this.userStore = userStore;
        this.matchManagerProvider = matchManagerProvider;
        this.teamManagerProvider = teamManagerProvider;
        this.matchProvider = matchProvider;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(UserLoginEvent event) {
        TeamManager teamManager = teamManagerProvider.get();

        Entrant participation = teamManager.getEntrant(event.getUser());
        if(participation == null) return;

        final int playerCount = (int) onlinePlayers.all()
                                                   .stream()
                                                   .filter(player -> !player.equals(event.getPlayer()) &&
                                                                     participation.members().contains(userStore.playerId(player)))
                                                   .count();

        int maxPlayers = tournament.max_players_per_match();
        if(playerCount >= maxPlayers) {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, new Component(new TranslatableComponent("tourney.kick.maxPlayers", String.valueOf(maxPlayers)), ChatColor.RED));
        } else {
            if(event.getResult().equals(PlayerLoginEvent.Result.KICK_WHITELIST)) {
                event.allow(); // explicitly allow players on teams but not whitelist
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void addPlayersToRegisteredTeam(EntrantRegisterEvent event) throws EventException {
        event.yield();

        Match match = event.getTeam().getMatch();
        TeamMatchModule tmm = match.needMatchModule(TeamMatchModule.class);

        for(MatchPlayer player : match.getPlayers()) {
            if(event.getEntrant().members().contains(player.getPlayerId())) {
                tourney.getLogger().info("Adding player '" + player.getDisplayName() + "' to team '" + event.getTeam().getName() + "'");
                tmm.forceJoin(player, event.getTeam());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onParticipationUnregister(EntrantUnregisterEvent event) {
        for (MatchPlayer player : event.getTeam().getPlayers()) {
            player.getBukkit().kickPlayer(
                    ChatColor.RED + "You are no longer competing on this tournament server.\n" +
                            ChatColor.RED + "Thanks for participating!"
            );
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void recordMatch(MatchEndEvent event) {
        Match match = event.getMatch();
        if (match.matchState().equals(MatchState.Finished)) {
            matchManagerProvider.get().recordMatchIfQueued();
        }
    }

    @EventHandler
    public void registerKDM(TourneyStateChangeEvent event) {
        if (tourney.getConfig().getBoolean("kdm-enabled", false)) {
            TourneyState state = event.getNewState();
            if(tourney.getKDMSession() == null && state.equals(TourneyState.ENABLED_WAITING_FOR_READY)) {
                Match match = matchProvider.get();
                if(match.getMatchModule(ScoreMatchModule.class) != null || match.module(BlitzMatchModuleImpl.class).filter(BlitzMatchModule::activated).isPresent()) {
                    tourney.createKDMSession();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handleMatchJoin(final MatchPlayerAddEvent event) {
        if (!tourney.getState().equals(TourneyState.ENABLED_FINISHED)) {
            final MatchManager matchManager = matchManagerProvider.get();
            if(matchManager.getMatch().equals(event.getMatch())) {
                final Player player = event.getPlayer().getBukkit();
                Team team = teamManagerProvider.get().getTeam(player);
                if(team != null) {
                    event.getMatch().setPlayerParty(event.getPlayer(), team, false);
                }
            }
        }
    }

    @EventHandler
    public void queueRecord(TourneyStateChangeEvent event) {
        if (event.getNewState().equals(TourneyState.ENABLED_RUNNING)) {
            tourney.setRecordQueued(true);
        }
    }
}
