package tc.oc.pgm.filters.matcher.player;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import tc.oc.pgm.playerstats.StatsUserFacet;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.filters.query.IPlayerQuery;

public class KillStreakFilter extends SpawnedPlayerFilter {
    private final @Inspect Range<Integer> range;
    private final @Inspect boolean repeat;
    private final @Inspect boolean persistent;

    public KillStreakFilter(Range<Integer> range, boolean repeat, boolean persistent) {
        this.range = range;
        this.repeat = repeat;
        this.persistent = persistent;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    protected boolean matches(IPlayerQuery query, MatchPlayer player) {
        final StatsUserFacet facet = player.getUserContext().facet(StatsUserFacet.class);
        int kills = persistent ? facet.teamKills() : facet.lifeKills();
        if(repeat && kills > 0) {
            int modulo = this.range.upperEndpoint() - (this.range.upperBoundType() == BoundType.CLOSED ? 0 : 1);
            kills = 1 + (kills - 1) % modulo;
        }
        return this.range.contains(kills);
    }
}
