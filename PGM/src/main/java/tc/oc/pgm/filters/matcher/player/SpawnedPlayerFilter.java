package tc.oc.pgm.filters.matcher.player;

import tc.oc.pgm.filters.matcher.TypedFilter;
import tc.oc.pgm.filters.query.IPlayerQuery;
import tc.oc.pgm.match.MatchPlayer;

/**
 * Base for filters that apply to *online*, participating players. The base class
 * returns DENY if the player is currently offline or observing, and abstains from
 * non-player queries.
 *
 * This should only be inherited by filters that absolutely require an online
 * {@link MatchPlayer} to match against. Generally, player filters should not rely
 * on the player's current state, and instead use only the properties of the
 * {@link IPlayerQuery} itself.
 *
 */
public abstract class SpawnedPlayerFilter extends TypedFilter.Impl<IPlayerQuery> {

    protected abstract boolean matches(IPlayerQuery query, MatchPlayer player);

    @Override
    public boolean matches(IPlayerQuery query) {
        return query.participant(query.getPlayerId())
                    .filter(player -> matches(query, player))
                    .isPresent();
    }
}
