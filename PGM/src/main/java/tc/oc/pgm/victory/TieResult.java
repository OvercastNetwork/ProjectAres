package tc.oc.pgm.victory;

import net.md_5.bungee.api.chat.BaseComponent;
import tc.oc.commons.core.chat.Component;
import tc.oc.pgm.match.Competitor;

/**
 * Force the match to end immediately with all competitors ranked equally.
 */
public class TieResult implements MatchResult {

    @Override
    public int compare(Competitor a, Competitor b) {
        return 0;
    }

    @Override
    public boolean isDefinite() {
        return true;
    }

    @Override
    public BaseComponent describeResult() {
        return new Component("nobody wins");
    }
}
