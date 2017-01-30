package tc.oc.pgm.countdowns;

import java.time.Duration;

public interface Countdown {

    default void onStart(Duration remaining, Duration total) {}

    default void onTick(Duration remaining, Duration total) {}

    default void onCancel(Duration remaining, Duration total, boolean manual) {}

    void onEnd(Duration total);
}
