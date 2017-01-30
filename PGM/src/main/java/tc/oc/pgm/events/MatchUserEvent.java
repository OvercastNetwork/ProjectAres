package tc.oc.pgm.events;

import java.util.UUID;
import java.util.stream.Stream;

public interface MatchUserEvent {
    Stream<UUID> users();
}
