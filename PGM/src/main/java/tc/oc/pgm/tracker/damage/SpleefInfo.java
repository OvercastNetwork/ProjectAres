package tc.oc.pgm.tracker.damage;

import javax.annotation.Nullable;

import tc.oc.commons.core.inspect.Inspectable;
import tc.oc.pgm.match.ParticipantState;
import tc.oc.pgm.time.TickTime;

import static com.google.common.base.Preconditions.checkNotNull;

public class SpleefInfo extends Inspectable.Impl implements DamageInfo, CauseInfo {

    @Inspect private final DamageInfo breaker;
    @Inspect private final TickTime time;

    public SpleefInfo(DamageInfo breaker, TickTime time) {
        this.breaker = checkNotNull(breaker);
        this.time = checkNotNull(time);
    }

    @Override
    public @Nullable ParticipantState getAttacker() {
        return getBreaker().getAttacker();
    }

    @Override
    public DamageInfo getCause() {
        return breaker;
    }

    public DamageInfo getBreaker() {
        return breaker;
    }

    public TickTime getTime() {
        return time;
    }
}
