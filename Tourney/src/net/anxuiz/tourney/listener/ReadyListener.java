package net.anxuiz.tourney.listener;

import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.sk89q.minecraft.util.commands.ChatColor;
import net.anxuiz.tourney.MatchManager;
import net.anxuiz.tourney.ReadyManager;
import net.anxuiz.tourney.Tourney;
import net.anxuiz.tourney.TourneyState;
import net.anxuiz.tourney.event.PartyReadyStatusChangeEvent;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tc.oc.api.docs.Tournament;
import tc.oc.pgm.countdowns.Countdown;
import tc.oc.pgm.countdowns.CountdownContext;
import tc.oc.pgm.events.MatchBeginEvent;
import tc.oc.pgm.events.PartyRemoveEvent;
import tc.oc.pgm.events.PlayerChangePartyEvent;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.Party;
import tc.oc.pgm.start.StartCountdown;
import tc.oc.pgm.start.StartMatchModule;

@Singleton
public class ReadyListener implements Listener {

    private final Tourney tourney;
    private final Provider<MatchManager> matchManagerProvider;
    private final Provider<Optional<ReadyManager>> readyManagerProvider;
    private final Provider<Tournament> tournamentProvider;

    @Inject ReadyListener(Tourney tourney, Provider<MatchManager> matchManagerProvider, Provider<Optional<ReadyManager>> readyManagerProvider, Provider<Tournament> tournamentProvider) {
        this.tourney = tourney;
        this.matchManagerProvider = matchManagerProvider;
        this.readyManagerProvider = readyManagerProvider;
        this.tournamentProvider = tournamentProvider;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLeave(PlayerChangePartyEvent event) throws EventException {
        if(event.getNewParty() != null) return;

        event.yield();

        ReadyManager readyManager = readyManagerProvider.get().orElse(null);
        if (readyManager == null) return;

        Tournament tournament = tournamentProvider.get();
        Party party = event.getOldParty();
        if (party != null && party.isParticipatingType() && party.getPlayers().size() < tournament.min_players_per_match()) {
            readyManager.markNotReady(party);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPartyRemove(PartyRemoveEvent event) {
        ReadyManager readyManager = readyManagerProvider.get().orElse(null);
        if(readyManager == null) return;

        readyManager.remove(event.getParty());
        checkMatchStart(event.getMatch());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPartyReadyChange(PartyReadyStatusChangeEvent event) {
        ReadyManager readyManager = readyManagerProvider.get().orElse(null);
        if (readyManager == null) return;

        Party party = event.getParty();
        boolean status = event.getNewStatus();
        Match match = party.getMatch();

        match.sendMessage(String.format("%s %s%sis %s", party.getColoredName(), ChatColor.RESET.toString(), ChatColor.YELLOW, status ? "now ready" : "no longer ready"));

        checkMatchStart(match);
    }

    void checkMatchStart(Match match) {
        ReadyManager readyManager = readyManagerProvider.get().get();
        CountdownContext countdownContext = match.countdowns();

        if(readyManager.readyToStart()) {
            match.needMatchModule(StartMatchModule.class).forceStartCountdown();
            tourney.setState(TourneyState.ENABLED_STARTING);
        } else {
            tourney.setState(TourneyState.ENABLED_WAITING_FOR_READY);

            int canceled = 0;
            for (Countdown countdown : countdownContext.getAll(StartCountdown.class)) {
                countdownContext.cancel(countdown);
                canceled++;
            }
            if (canceled > 0) {
                match.sendMessage(ChatColor.RED + "Match start countdown cancelled because some teams are not ready");
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMatchBegin(MatchBeginEvent event) {
        matchManagerProvider.get().clearReadyManager();
    }
}
