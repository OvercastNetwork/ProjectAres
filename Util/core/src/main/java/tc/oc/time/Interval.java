package tc.oc.time;

import java.time.Instant;

/**
 * TODO: expand this?
 */
public class Interval {

    private final Instant start;
    private final Instant end;

    private Interval(Instant start, Instant end) {
        this.start = start;
        this.end = end;
    }

    public static Interval between(Instant start, Instant end) {
        return new Interval(start, end);
    }

    public Instant getStart() {
        return start;
    }

    public Instant getEnd() {
        return end;
    }
}
