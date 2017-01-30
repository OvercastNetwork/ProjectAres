package tc.oc.pgm.timelimit;

import javax.annotation.Nullable;

import java.time.Duration;

import tc.oc.commons.core.util.Comparables;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.victory.AbstractVictoryCondition;
import tc.oc.pgm.victory.DefaultResult;
import tc.oc.pgm.victory.MatchResult;
import tc.oc.pgm.victory.VictoryCondition;
import tc.oc.pgm.victory.VictoryMatchModule;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * A time limit configuration in the context of a specific match.
 *
 * It can be created from a {@link TimeLimitDefinition}, or from a manually entered command.
 * In either case, it has the same immutable parameters as a definition.
 *
 * This class is responsible for creating and starting {@link TimeLimitCountdown}s.
 * It tracks the current countdown, if any, and whether it has completed.
 * It uses this state to implement {@link VictoryCondition}.
 *
 * A single {@link TimeLimit} can potentially be reused, if its countdown is cancelled.
 *
 * @see TimeLimitDefinition
 * @see TimeLimitCountdown
 */
public class TimeLimit extends AbstractVictoryCondition {

    public static final Duration MAX_DURATION = Duration.ofDays(99999);

    private final Match match;
    private final Duration duration;
    private final boolean show;
    private final DefaultResult defaultResult = new DefaultResult();

    private @Nullable TimeLimitCountdown countdown;
    private boolean finished;

    public TimeLimit(Match match, Duration duration, MatchResult result, boolean show) {
        super(Priority.TIME_LIMIT, result);
        checkArgument(Comparables.lessOrEqual(duration, MAX_DURATION));
        this.match = checkNotNull(match);
        this.duration = checkNotNull(duration);
        this.show = show;
    }

    public TimeLimit(Match match, TimeLimitDefinition definition) {
        this(match, definition.duration(), definition.result(), definition.show());
    }

    public Duration getDuration() {
        return duration;
    }

    public boolean getShow() {
        return show;
    }

    public void start() {
        checkState(!finished);
        checkState(countdown == null);
        countdown = new TimeLimitCountdown(match, this);
        match.countdowns().start(countdown, duration);
    }

    void onCancel(boolean manual) {
        if(!finished) {
            countdown = null;
            if(manual) {
                match.needMatchModule(TimeLimitMatchModule.class).setTimeLimit(null);
            }
        }
    }

    void onEnd() {
        finished = true;
        match.needMatchModule(VictoryMatchModule.class).invalidateAndCheckEnd();
    }

    public @Nullable Duration remaining() {
        return countdown == null ? null : countdown.remaining();
    }

    @Override
    public boolean isCompleted() {
        return finished;
    }

    @Override
    public MatchResult result() {
        return finished ? super.result() : defaultResult;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + duration + " result=" + result() + "}";
    }
}
