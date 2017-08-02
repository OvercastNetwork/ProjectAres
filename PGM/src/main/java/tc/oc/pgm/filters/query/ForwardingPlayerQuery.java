package tc.oc.pgm.filters.query;

import java.util.Optional;

import org.bukkit.EntityLocation;
import tc.oc.api.docs.PlayerId;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchPlayerState;
import tc.oc.pgm.match.ParticipantState;
import tc.oc.pgm.match.Party;

public interface ForwardingPlayerQuery extends IPlayerQuery {

    IPlayerQuery playerQuery();

    @Override
    default Match getMatch() {
        return playerQuery().getMatch();
    }

    @Override
    default Party getParty() {
        return playerQuery().getParty();
    }

    @Override
    default PlayerId getPlayerId() {
        return playerQuery().getPlayerId();
    }

    @Override
    default MatchPlayerState playerState() {
        return playerQuery().playerState();
    }

    @Override
    default Optional<ParticipantState> participantState() {
        return playerQuery().participantState();
    }

    @Override
    default Optional<MatchPlayer> onlinePlayer() {
        return playerQuery().onlinePlayer();
    }

    @Override
    default EntityLocation getEntityLocation() {
        return playerQuery().getEntityLocation();
    }
}
