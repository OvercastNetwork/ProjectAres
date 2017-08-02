package tc.oc.pgm.victory;

import java.util.Comparator;
import java.util.Set;
import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import org.bukkit.event.EventBus;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tc.oc.commons.core.util.RankedSet;
import tc.oc.pgm.events.CompetitorAddEvent;
import tc.oc.pgm.events.CompetitorRemoveEvent;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.MatchEndEvent;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchScope;

/**
 * Decides when the match should end and who should win
 *
 * Regression tests:
 *
 *     - In a goals + blitz match, a team that completes all their goals should win,
 *       even if they have fewer players than other teams.
 *     - In a goals + blitz match, a team that runs out of players should lose,
 *       even if they have more goals completed.
 *     - When a time limit elapses with a default result, it should end the match,
 *       but not affect the final ranking of competitors.
 *     - When a time limit elapses with a tie result, no team should win, even if
 *       some teams are leading according to other conditions.
 */
@ListenerScope(MatchScope.LOADED)
public class VictoryMatchModule extends MatchModule implements Listener {

    @Inject private EventBus eventBus;

    private final VictoryCalculator calculator = new VictoryCalculator();

    @Override
    public void load() {
        super.load();
        calculator().addCompetitors(match.getCompetitors());
        invalidateCompetitorRanking();
    }

    @Override
    public void enable() {
        super.enable();
        match.getScheduler(MatchScope.RUNNING)
             .createTask(this::invalidateAndCheckEnd);
    }

    public VictoryCalculator calculator() {
        return calculator;
    }

    public Comparator<Competitor> victoryOrder() {
        return calculator().victoryOrder();
    }

    public void setImmediateWinner(Competitor competitor) {
        calculator().setImmediateWinner(competitor);
    }

    public void setVictoryCondition(VictoryCondition condition) {
        calculator().setVictoryCondition(condition);
    }

    /**
     * Re-sort all {@link Competitor}s from scratch, according to the current {@link VictoryCondition}.
     * This should be called when the state of the match changes in a way that affects the
     * rank of any competitors.
     */
    public void invalidateCompetitorRanking() {
        final ImmutableList<Competitor> before = ImmutableList.copyOf(calculator().rankedCompetitors());
        calculator().invalidateRanking();
        final ImmutableList<Competitor> after = ImmutableList.copyOf(calculator().rankedCompetitors());

        if(!before.equals(after)) {
            eventBus.callEvent(new RankingsChangeEvent(match, before, after));
        }
    }

    /**
     * Return all currently active competitors, ordered by closeness to winning the match.
     * Competitors that are tied are returned in arbitrary, and inconsistent order.
     */
    public RankedSet<Competitor> rankedCompetitors() {
        return calculator().rankedCompetitors();
    }

    /**
     * Return all {@link Competitor}s that are as close as, or closer to winning
     * the match than any other competitor. Unless the match is empty, the returned
     * set will always contain at least one competitor. If all competitors are tied
     * for the lead, the returned set will contain all of them.
     */
    public Set<Competitor> leaders() {
        return calculator().leaders();
    }

    /**
     * If some competitors finished ahead of others, return the winners,
     * otherwise return an empty set.
     */
    public Set<Competitor> winners() {
        return calculator().winners();
    }

    public boolean shouldMatchEnd() {
        return match.isFinished() || (match.hasStarted() &&
                                      calculator().shouldMatchEnd());
    }

    public boolean checkMatchEnd() {
        if(match.hasStarted() && !match.isFinished() && shouldMatchEnd()) {
            match.end();
            return true;
        }
        return false;
    }

    public boolean invalidateAndCheckEnd() {
        invalidateCompetitorRanking();
        return checkMatchEnd();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCompetitorAddEarly(CompetitorAddEvent event) {
        calculator().addCompetitor(event.getCompetitor());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCompetitorAddLate(CompetitorAddEvent event) {
        invalidateCompetitorRanking();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCompetitorRemove(CompetitorRemoveEvent event) {
        calculator().removeCompetitor(event.getCompetitor());
        invalidateCompetitorRanking();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMatchEnd(MatchEndEvent event) {
        invalidateCompetitorRanking();
    }
}
