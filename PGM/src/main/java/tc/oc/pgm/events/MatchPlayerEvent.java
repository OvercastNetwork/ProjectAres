package tc.oc.pgm.events;

import java.util.UUID;
import java.util.stream.Stream;

import tc.oc.pgm.match.MatchPlayer;

public interface MatchPlayerEvent extends MatchUserEvent {

    Stream<MatchPlayer> players();

    @Override
    default Stream<UUID> users() {
        return players().map(MatchPlayer::getUniqueId);
    }
}
