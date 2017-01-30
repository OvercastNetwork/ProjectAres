package tc.oc.pgm.analytics;

import javax.inject.Inject;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tc.oc.analytics.Gauge;
import tc.oc.analytics.MetricFactory;
import tc.oc.commons.core.scheduler.Scheduler;
import tc.oc.minecraft.analytics.AnalyticsFacet;
import tc.oc.pgm.events.PlayerChangePartyEvent;
import tc.oc.pgm.match.MatchFinder;

public class MatchPlayerReporter extends AnalyticsFacet implements Listener {

    private final MatchFinder matchFinder;
    private final Scheduler scheduler;
    private final Gauge participants, observers;

    @Inject MatchPlayerReporter(MatchFinder matchFinder, Scheduler scheduler, MetricFactory metrics) {
        this.matchFinder = matchFinder;
        this.scheduler = scheduler;
        this.participants = metrics.gauge("players.participants");
        this.observers = metrics.gauge("players.observers");
    }

    private void update() {
        scheduler.debounceTask(() -> {
            participants.measure(matchFinder.currentMatches()
                                            .stream()
                                            .mapToInt(match -> match.getParticipatingPlayers().size())
                                            .sum());

            observers.measure(matchFinder.currentMatches()
                                         .stream()
                                         .mapToInt(match -> match.getObservingPlayers().size())
                                         .sum());
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void changeParty(PlayerChangePartyEvent event) {
        update();
    }
}
