package tc.oc.debug;

import java.time.Duration;
import java.time.Instant;

public interface LeakDetector {

    /**
     * Log an error if the given object is not released within the given amount of time from now
     */
    void expectRelease(Object obj, Duration within, boolean forceCollection);

    /**
     * Log an error if the given object is not released before the given time
     */
    void expectRelease(Object obj, Instant deadline, boolean forceCollection);
}
