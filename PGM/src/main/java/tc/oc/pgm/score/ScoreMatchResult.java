package tc.oc.pgm.score;

import net.md_5.bungee.api.chat.BaseComponent;
import tc.oc.commons.core.chat.Component;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.victory.MatchResult;

public class ScoreMatchResult implements MatchResult {
    @Override
    public int compare(Competitor a, Competitor b) {
        ScoreMatchModule smm = a.getMatch().needMatchModule(ScoreMatchModule.class);
        return Double.compare(smm.getScore(b), smm.getScore(a)); // reversed
    }

    @Override
    public BaseComponent describeResult() {
        return new Component("highest score");
    }
}
