package tc.oc.pgm.victory;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import tc.oc.commons.core.util.Comparators;
import tc.oc.commons.core.util.RankedSet;
import tc.oc.pgm.match.Competitor;

/**
 * This class contains the core logic of the VictoryMatchModule. It was factored out in
 * order to write tests for it, but that is still infeasible until we have a way to
 * mock {@link Competitor}, and probably Match as well.
 */
public class VictoryCalculator {
    /**
     * Conditions that determine the ranks of Competitors and the completion of the Match
     */
    private Set<VictoryCondition> victoryConditions = new HashSet<>();

    /**
     * Effective order in which VictoryConditions are asked to rank competitors.
     * The first non-zero answer wins.
     *
     * VictoryCondition that say the match is over take priority over those that don't.
     * After that, they are ordered by their priority property.
     */
    private final Comparator<VictoryCondition> victoryConditionOrder = Comparators
        .firstIf(VictoryCondition::isCompleted)
        .thenComparing(VictoryCondition::priority);

    /**
     * Ordering of Competitors by their closeness to victory.
     *
     * Obviously, this is not constant, so this comparator cannot be used in a sorted collection.
     */
    private final Comparator<Competitor> victoryOrder = (a, b) -> {
        // If there are any final conditions, one of them alone (arbitrarily chosen) determines the result.
        // Otherwise, the highest priority condition with a non-zero result is used.
        VictoryCondition topCondition = null;
        int topResult = 0;

        for(VictoryCondition condition : victoryConditions) {
            if(topCondition == null || victoryConditionOrder.compare(condition, topCondition) < 0) {
                final int result = condition.result().compare(a, b);
                if(result != 0 || condition.result().isDefinite()) {
                    topCondition = condition;
                    topResult = result;
                }
            }
        }
        return topResult;
    };

    private final RankedSet<Competitor> rankedCompetitors = new RankedSet<>(victoryOrder);

    public void addCompetitor(Competitor competitor) {
        rankedCompetitors.add(competitor);
    }

    public void addCompetitors(Collection<Competitor> competitors) {
        rankedCompetitors.addAll(competitors);
    }

    public void removeCompetitor(Competitor competitor) {
        rankedCompetitors.remove(competitor);
    }

    public void invalidateRanking() {
        rankedCompetitors.invalidateRanking();
    }

    public Comparator<Competitor> victoryOrder() {
        return victoryOrder;
    }

    public void setImmediateWinner(Competitor competitor) {
        setImmediateResult(new CompetitorResult(competitor));
    }

    public void setImmediateResult(MatchResult result) {
        setVictoryCondition(new ImmediateVictoryCondition(result));
    }

    public void setVictoryCondition(VictoryCondition condition) {
        setVictoryCondition((Class<VictoryCondition>) condition.getClass(), condition);
    }

    public <T extends VictoryCondition> void setVictoryCondition(Class<T> type, @Nullable T condition) {
        removeVictoryConditions(type);
        if(condition != null && victoryConditions.add(condition)) {
            invalidateRanking();
        }
    }

    public void removeVictoryConditions(Class<? extends VictoryCondition> type) {
        boolean changed = false;
        for(Iterator<VictoryCondition> iterator = victoryConditions.iterator(); iterator.hasNext(); ) {
            VictoryCondition condition = iterator.next();
            if(type.isInstance(condition)) {
                iterator.remove();
                changed = true;
            }
        }
        if(changed) invalidateRanking();
    }

    /**
     * Return all currently active competitors, ordered by closeness to winning the match.
     * Competitors that are tied are returned in arbitrary, and inconsistent order.
     */
    public RankedSet<Competitor> rankedCompetitors() {
        return rankedCompetitors;
    }

    public boolean shouldMatchEnd() {
        return victoryConditions.stream().anyMatch(VictoryCondition::isCompleted);
    }

    /**
     * Return all {@link Competitor}s that are as close as, or closer to winning
     * the match than any other competitor. Unless the match is empty, the returned
     * set will always contain at least one competitor. If all competitors are tied
     * for the lead, the returned set will contain all of them.
     */
    public Set<Competitor> leaders() {
        return rankedCompetitors.getRank(0);
    }

    /**
     * There are winners if there is only one competitor, or there
     * are multiple competitors and some are ranked higher than others.
     */
    public boolean hasWinners() {
        return leaders().size() > 0 && leaders().size() < Math.max(2, rankedCompetitors().size());
    }

    /**
     * If some competitors finished ahead of others, return the winners,
     * otherwise return an empty set.
     */
    public Set<Competitor> winners() {
        return hasWinners() ? leaders()
                            : ImmutableSet.of();
    }

    /**
     * If some competitors finished behind others, return those that did not win,
     * otherwise return an empty set.
     */
    public Set<Competitor> losers() {
        return hasWinners() ? Sets.difference(rankedCompetitors(), leaders())
                            : ImmutableSet.of();
    }
}
