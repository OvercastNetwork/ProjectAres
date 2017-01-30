package tc.oc.pgm.quota;

import java.time.Duration;
import tc.oc.pgm.match.MatchPlayer;

public interface Quota {
    boolean appliesTo(MatchPlayer player);
    int priority();
    Duration interval();
    int maximum();
    boolean premium();
}
