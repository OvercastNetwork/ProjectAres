package tc.oc.pgm.match;

import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * The next (unused) match serial number, incremented after the match loads
 */
@Singleton
public class MatchCounter extends AtomicInteger {

    @Inject MatchCounter() {
        super(1);
    }
}
