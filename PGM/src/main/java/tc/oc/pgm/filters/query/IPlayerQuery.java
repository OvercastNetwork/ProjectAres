package tc.oc.pgm.filters.query;

import java.util.Optional;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import tc.oc.api.docs.PlayerId;
import tc.oc.commons.core.util.Optionals;
import tc.oc.pgm.filters.Filterable;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchPlayerState;
import tc.oc.pgm.match.ParticipantState;

/**
 * A query for a player that may or may not be online or participating in the match.
 */
public interface IPlayerQuery extends IPartyQuery, IEntityQuery {

    PlayerId getPlayerId();

    /**
     * Return the {@link MatchPlayer} for this player if they are online,
     * AND still a member of the party returned from {@link #getParty()}.
     */
    Optional<MatchPlayer> onlinePlayer();

    MatchPlayerState playerState();

    Optional<ParticipantState> participantState();

    @Override
    default <Q extends Filterable<?>> Optional<? extends Q> filterable(Class<Q> type) {
        return Optionals.first(Optionals.cast(onlinePlayer(), type),
                               IPartyQuery.super.filterable(type));
    }

    @Override
    default Class<? extends Entity> getEntityType() {
        return Player.class;
    }

    @Override
    default int randomSeed() {
        return getPlayerId().hashCode();
    }
}
