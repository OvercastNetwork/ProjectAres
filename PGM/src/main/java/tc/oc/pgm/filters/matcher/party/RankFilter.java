package tc.oc.pgm.filters.matcher.party;

import com.google.common.collect.Range;
import tc.oc.pgm.filters.query.IMatchQuery;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.victory.VictoryMatchModule;

/**
 * Match whether a {@link Competitor}'s rank is within a range.
 */
public class RankFilter extends CompetitorFilter {

    private final @Inspect Range<Integer> positions;

    public RankFilter(Range<Integer> positions) {
        this.positions = positions;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public boolean matches(IMatchQuery query, Competitor competitor) {
        return positions.contains(competitor.getMatch().needMatchModule(VictoryMatchModule.class).rankedCompetitors().getPosition(competitor));
    }
}
