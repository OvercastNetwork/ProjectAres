package tc.oc.pgm.blitz;

import net.md_5.bungee.api.chat.BaseComponent;
import tc.oc.commons.core.chat.Component;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.victory.MatchResult;

public class BlitzMatchResult implements MatchResult {

    @Override
    public int compare(Competitor a, Competitor b) {
        return Integer.compare(b.getPlayers().size(), a.getPlayers().size());
    }

    @Override
    public BaseComponent describeResult() {
        return new Component("most survivors");
    }

}
