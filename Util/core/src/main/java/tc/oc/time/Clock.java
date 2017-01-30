package tc.oc.time;

import java.time.Instant;

/**
 * Provider of the current time
 *
 * NOTE: Try to keep this compatible with {@link java.time.Clock},
 * in case we switch to JDK time at some point.
 */
public interface Clock {
    Instant instant();
}
