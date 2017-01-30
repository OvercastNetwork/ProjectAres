package tc.oc.pgm.goals;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Multimap;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.victory.AbstractVictoryCondition;

// TODO: Break this down into multiple chainable conditions i.e. completions, touches, proximity, etc.
public class GoalsVictoryCondition extends AbstractVictoryCondition {

    private final Multimap<Competitor, Goal> goalsByCompetitor;

    public GoalsVictoryCondition(Multimap<Competitor, Goal> goalsByCompetitor) {
        super(Priority.GOALS, new GoalsMatchResult());
        this.goalsByCompetitor = goalsByCompetitor;
    }

    @Override
    public boolean isCompleted() {
        competitors: for(Map.Entry<Competitor, Collection<Goal>> entry : goalsByCompetitor.asMap().entrySet()) {
            boolean someRequired = false;
            for(Goal<?> goal : entry.getValue()) {
                if(goal.isRequired()) {
                    // If any required goals are incomplete, skip to the next competitor
                    if(!goal.isCompleted(entry.getKey())) continue competitors;
                    someRequired = true;
                }
            }
            // If some goals are required, and they are all complete, competitor wins the match
            if(someRequired) return true;
        }
        // If no competitors won, match is not over
        return false;
    }
}
