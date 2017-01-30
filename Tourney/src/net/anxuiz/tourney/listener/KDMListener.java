package net.anxuiz.tourney.listener;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.sk89q.minecraft.util.commands.ChatColor;
import net.anxuiz.tourney.KDMSession;
import net.anxuiz.tourney.TeamManager;
import net.anxuiz.tourney.Tourney;
import net.anxuiz.tourney.TourneyState;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import java.time.Duration;

import tc.oc.api.docs.Entrant;
import tc.oc.pgm.cycle.CycleMatchModule;
import tc.oc.pgm.events.CycleEvent;
import tc.oc.pgm.events.MatchEndEvent;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchState;
import tc.oc.pgm.victory.VictoryMatchModule;

public class KDMListener implements Listener {
    private KDMSession session;

    public KDMListener(final KDMSession session) {
        this.session = Preconditions.checkNotNull(session, "Session");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMatchCycle(CycleEvent event) {
        Tourney plugin = Tourney.get();
        if (event.getOldMatch().matchState().equals(MatchState.Finished)) {
            plugin.setState(TourneyState.ENABLED_WAITING_FOR_TEAMS);
            TeamManager teamManager = plugin.getMatchManager().getTeamManager();
            teamManager.assignTeams(this.session.getEntrants());
        } else {
            plugin.clearKDMSession();
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onMatchEnd(MatchEndEvent event) {
        Match match = event.getMatch();
        Tourney plugin = Tourney.get();
        this.session.appendMatch(match, plugin.getMatchManager().getTeamManager().teamToEntrant(Iterables.getOnlyElement(event.getMatch().needMatchModule(VictoryMatchModule.class).winners(), null)));

        Entrant winningParticipation = this.session.calculateWinner();
        int matchesPlayed = this.session.getMatchesPlayed();
        if (winningParticipation != null) {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "A winner has been determined!");
            Bukkit.broadcastMessage(ChatColor.AQUA + WordUtils.capitalize(winningParticipation.team().name()) + ChatColor.RESET + ChatColor.YELLOW + " wins! Congratulations!");
            plugin.clearKDMSession();
        } else if (matchesPlayed < 3) {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "A winner has not yet been determined! Beginning match #" + (matchesPlayed + 1) + "...");
            match.needMatchModule(CycleMatchModule.class).startCountdown(Duration.ofSeconds(15), session.getMap());
        } else {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "There is a tie! Congratulations to both teams!");
            Tourney.get().clearKDMSession();
        }
    }
}
