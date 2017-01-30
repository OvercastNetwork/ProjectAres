package tc.oc.pgm.timelimit;

import java.util.Objects;
import javax.annotation.Nullable;

import java.time.Duration;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.victory.VictoryCalculator;
import tc.oc.pgm.victory.VictoryMatchModule;

public class TimeLimitMatchModule extends MatchModule {
    private @Nullable final TimeLimit defaultTimeLimit;
    private @Nullable TimeLimit timeLimit;

    public TimeLimitMatchModule(Match match, @Nullable TimeLimitDefinition defaultTimeLimitDefinition) {
        super(match);
        this.defaultTimeLimit = defaultTimeLimitDefinition == null ? null : new TimeLimit(match, defaultTimeLimitDefinition);
    }

    @Override
    public void load() {
        super.load();
        setTimeLimit(defaultTimeLimit);
    }

    @Override
    public void enable() {
        start();
    }

    public @Nullable TimeLimit getTimeLimit() {
        return this.timeLimit;
    }

    public void setTimeLimit(@Nullable TimeLimit timeLimit) {
        if(!Objects.equals(this.timeLimit, timeLimit)) {
            logger.fine("Changing time limit to " + timeLimit);
            match.countdowns().cancelAll(TimeLimitCountdown.class);
            this.timeLimit = timeLimit;
            match.needMatchModule(VictoryMatchModule.class)
                 .calculator()
                 .setVictoryCondition(TimeLimit.class, timeLimit);
        }
    }

    public @Nullable Duration remaining() {
        return timeLimit == null ? null : timeLimit.remaining();
    }

    public void start() {
        // Match.end() will cancel this, so we don't have to
        if(timeLimit != null && match.isRunning()) {
            timeLimit.start();
        }
    }
}
