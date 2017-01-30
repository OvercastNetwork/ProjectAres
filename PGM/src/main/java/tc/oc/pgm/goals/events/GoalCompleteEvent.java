package tc.oc.pgm.goals.events;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;
import tc.oc.pgm.goals.Contribution;
import tc.oc.pgm.goals.Goal;
import tc.oc.pgm.match.Competitor;

public class GoalCompleteEvent<T extends Goal> extends GoalEvent<T> {

    private final boolean completed;
    private final Predicate<Competitor> wasCompletedFor, isCompletedFor;
    private final ImmutableList<? extends Contribution> contributions;

    public GoalCompleteEvent(T goal, boolean completed, Predicate<Competitor> wasCompletedFor, Predicate<Competitor> isCompletedFor) {
        this(goal, completed, wasCompletedFor, isCompletedFor, Collections.emptyList());
    }

    public GoalCompleteEvent(T goal, boolean completed, Predicate<Competitor> wasCompletedFor, Predicate<Competitor> isCompletedFor, List<? extends Contribution> contributions) {
        super(goal);
        this.completed = completed;
        this.wasCompletedFor = wasCompletedFor;
        this.isCompletedFor = isCompletedFor;
        this.contributions = ImmutableList.copyOf(contributions);
    }

    public ImmutableList<? extends Contribution> getContributions() {
        return contributions;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Predicate<Competitor> wasCompletedFor() {
        return wasCompletedFor;
    }

    public Predicate<Competitor> isCompletedFor() {
        return isCompletedFor;
    }

    public boolean wasCompletedFor(Competitor competitor) {
        return wasCompletedFor.test(competitor);
    }

    /**
     * @return true if the event was beneficial to the affected team, false if it was detrimental
     */
    public boolean isCompletedFor(Competitor competitor) {
        return isCompletedFor.test(competitor);
    }
}
