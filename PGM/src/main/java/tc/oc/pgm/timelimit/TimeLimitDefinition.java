package tc.oc.pgm.timelimit;

import java.time.Duration;
import tc.oc.pgm.features.FeatureDefinition;
import tc.oc.pgm.victory.MatchResult;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A time limit defined in XML, immutable and created at parse time.
 *
 * @see TimeLimit
 * @see TimeLimitCountdown
 */
public class TimeLimitDefinition extends FeatureDefinition.Impl implements FeatureDefinition {

    private final @Inspect Duration duration;
    private final @Inspect MatchResult result;
    private final @Inspect boolean show;

    public TimeLimitDefinition(Duration duration, MatchResult result, boolean show) {
        this.duration = checkNotNull(duration);
        this.result = checkNotNull(result);
        this.show = show;
    }

    public Duration duration() {
        return duration;
    }

    public MatchResult result() {
        return result;
    }

    public boolean show() {
        return show;
    }
}
