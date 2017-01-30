package tc.oc.pgm.filters.matcher.party;

import java.util.Optional;

import tc.oc.pgm.filters.query.IMatchQuery;
import tc.oc.pgm.goals.GoalDefinition;
import tc.oc.pgm.match.Competitor;

public class GoalFilter extends CompetitorFilter {
    private final @Inspect(brief = true) GoalDefinition goal;

    public GoalFilter(GoalDefinition goal) {
        this.goal = goal;
    }

    @Override
    public String inspectType() {
        return "Goal";
    }

    @Override
    public String toString() {
        return inspect();
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    public boolean matches(IMatchQuery query, Optional<Competitor> competitor) {
        return goal.getGoal(query.getMatch()).isCompleted(competitor);
    }

    @Override
    public boolean matchesAny(IMatchQuery query) {
        return matches(query, Optional.empty());
    }

    @Override
    public boolean matches(IMatchQuery query, Competitor competitor) {
        return matches(query, Optional.of(competitor));
    }
}
