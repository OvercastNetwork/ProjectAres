package tc.oc.time;

import java.time.Duration;
import java.time.temporal.TemporalAmount;

/**
 * Encapsulates the logic used to convert a {@link Duration} to a {@link TemporalAmount},
 * i.e. how to break the time down into individual units and quantities.
 */
public interface PeriodConverter {
    TemporalAmount toPeriod(Duration duration);
}

