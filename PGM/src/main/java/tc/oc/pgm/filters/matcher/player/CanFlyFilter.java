package tc.oc.pgm.filters.matcher.player;

import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.filters.query.IPlayerQuery;

public class CanFlyFilter extends SpawnedPlayerFilter {
    @Override
    protected boolean matches(IPlayerQuery query, MatchPlayer player) {
        return player.getBukkit().getAllowFlight();
    }
}
