package tc.oc.commons.core.scheduler;

import java.time.Duration;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Task that can only be scheduled if it is not already scheduled.
 */
public class DebouncedTask extends ReusableTask {

    private final Duration delay;

    DebouncedTask(Scheduler scheduler, Runnable runnable) {
        this(scheduler, Duration.ZERO, runnable);
    }

    DebouncedTask(Scheduler scheduler, Duration delay, Runnable runnable) {
        super(scheduler, runnable);
        this.delay = checkNotNull(delay);
    }

    public boolean schedule() {
        if(isQueued()) return false;
        schedule(Task.Parameters.after(delay));
        return true;
    }
}
