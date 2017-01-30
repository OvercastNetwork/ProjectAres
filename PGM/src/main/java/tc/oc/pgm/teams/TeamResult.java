package tc.oc.pgm.teams;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import tc.oc.commons.core.chat.Component;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.victory.MatchResult;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Force the match to end immediately with the given {@link Team} as the exclusive winner.
 * Other competitors may be ranked by other conditions, but none will be ahead of this team.
 */
public class TeamResult implements MatchResult {
    private TeamFactory team;

    public TeamResult(TeamFactory team) {
        this.team = checkNotNull(team);
    }

    @Override
    public int compare(Competitor a, Competitor b) {
        return Boolean.compare(team.equals(Teams.getDefinition(b)),
                               team.equals(Teams.getDefinition(a)));
    }

    @Override
    public BaseComponent describeResult() {
        return new TranslatableComponent(team.isDefaultNamePlural() ? "broadcast.gameOver.teamWinText.plural"
                                                                    : "broadcast.gameOver.teamWinText",
                                         new Component(team.getDefaultName(), team.getDefaultColor()));
    }
}
