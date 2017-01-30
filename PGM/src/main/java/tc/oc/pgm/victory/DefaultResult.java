package tc.oc.pgm.victory;

import net.md_5.bungee.api.chat.BaseComponent;
import tc.oc.commons.core.chat.Component;
import tc.oc.pgm.match.Competitor;

/**
 * Has no effect, i.e. falls through to other victory conditions
 */
public class DefaultResult implements MatchResult {
    @Override
    public int compare(Competitor a, Competitor b) {
        return 0;
    }

    @Override
    public BaseComponent describeResult() {
        return new Component("default");
    }
}
