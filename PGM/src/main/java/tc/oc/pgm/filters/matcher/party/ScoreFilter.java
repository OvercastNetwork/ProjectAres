package tc.oc.pgm.filters.matcher.party;

import com.google.common.collect.Range;
import tc.oc.pgm.filters.query.IMatchQuery;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.score.ScoreMatchModule;

/**
 * Match whether a {@link Competitor}'s score is within a range.
 */
public class ScoreFilter extends CompetitorFilter {

    private final @Inspect Range<Integer> scores;

    public ScoreFilter(Range<Integer> scores) {
        this.scores = scores;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public boolean matches(IMatchQuery query, Competitor competitor) {
        return competitor.getMatch()
                         .module(ScoreMatchModule.class)
                         .filter(smm -> scores.contains((int) smm.getScore(competitor)))
                         .isPresent();
    }
}
