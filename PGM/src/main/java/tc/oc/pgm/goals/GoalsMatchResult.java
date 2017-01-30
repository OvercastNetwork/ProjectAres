package tc.oc.pgm.goals;

import net.md_5.bungee.api.chat.BaseComponent;
import tc.oc.commons.core.chat.Component;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.victory.MatchResult;

public class GoalsMatchResult implements MatchResult {
    @Override
    public int compare(Competitor a, Competitor b) {
        return a.getMatch()
                .needMatchModule(GoalMatchModule.class)
                .compareProgress(a, b);
    }

    @Override
    public BaseComponent describeResult() {
        return new Component("most objectives");
    }
}
