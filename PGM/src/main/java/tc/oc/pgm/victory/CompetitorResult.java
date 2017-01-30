package tc.oc.pgm.victory;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import tc.oc.pgm.match.Competitor;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Force the match to end immediately with the given {@link Competitor} as the exclusive winner.
 * Other competitors may be ranked by other conditions, but none will be ahead of this one.
 */
public class CompetitorResult implements MatchResult {

    private final Competitor competitor;

    public CompetitorResult(Competitor competitor) {
        this.competitor = checkNotNull(competitor);
    }

    @Override
    public int compare(Competitor a, Competitor b) {
        return Boolean.compare(competitor.equals(b), competitor.equals(a));
    }

    @Override
    public BaseComponent describeResult() {
        return new TranslatableComponent(competitor.isNamePlural() ? "broadcast.gameOver.teamWinText.plural"
                                                                   : "broadcast.gameOver.teamWinText",
                                         competitor.getComponentName());
    }
}
