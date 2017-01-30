package tc.oc.commons.core.scheduler;

import java.util.Objects;
import javax.annotation.Nullable;

import java.time.Duration;
import java.time.Instant;
import tc.oc.commons.core.util.TimeUtils;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a task to be managed by a {@link Scheduler}.
 *
 * A task can be instantiated but not be running. This allows tasks to be
 * queued before certain events occur.
 *
 * This is *only* to be constructed by a {@link Scheduler}. Constructing this
 * without the help of the scheduler will most likely cause bugs.
 */
public interface Task {

    /**
     * Get the scheduler for this task.
     * @return the scheduler.
     */
    Scheduler getScheduler();

    /**
     * Get the parameters that will tell the scheduler how to run this task.
     * @return the task parameters.
     */
    Parameters getParameters();

    /**
     * Get the runnable for this task. Do not attempt to directly interrupt this,
     * instead use {@link #cancel()}.
     * @return the runnable for this task.
     */
    Runnable getRunnable();

    /**
     * Get whether this task is currently running.
     * @return whether the task is running.
     */
    boolean isRunning();

    /**
     * Get whether this task is queued to run.
     * @return whether the task is queued.
     */
    boolean isQueued();

    /**
     * Get whether this task is registered, queued, or running.
     * @return whether the task is pending.
     */
    boolean isPending();

    /**
     * Cancel this task from ever running again.
     * If the task was never started, it will be cancelled
     * instantly when it is started.
     */
    void cancel();

    /**
     * Parameters for running a task.
     */
    class Parameters {

        private final Instant creation;
        private @Nullable Duration delay;
        private @Nullable Duration interval;

        private Parameters(Instant creation, @Nullable Duration delay, @Nullable Duration interval) {
            this.creation = creation;
            this.delay = delay;
            this.interval = interval;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{delay=" + delay() + ", interval=" + interval() + "}";
        }

        public @Nullable Duration delay() {
            return delay;
        }

        public @Nullable Duration interval() {
            return interval;
        }

        public boolean isRepeating() {
            return interval() != null;
        }

        public Instant creation() {
            return creation;
        }

        public Instant start() {
            return delay == null ? creation : creation.plus(delay);
        }

        public static Parameters fromDuration(@Nullable Duration delay, @Nullable Duration interval) {
            return new Parameters(Instant.now(), delay, interval);
        }

        public static Parameters fromInstant(@Nullable Instant start, @Nullable Duration interval) {
            final Instant now = Instant.now();
            final Duration delay = start != null && start.isAfter(now) ? Duration.between(now, start) : null;
            return new Parameters(now, delay, interval);
        }

        public static Parameters fromInstant(@Nullable Instant start) {
            return fromInstant(start, null);
        }

        public static Parameters fromTicks(@Nullable Long delay, @Nullable Long interval) {
            return fromDuration(delay == null ? null : TimeUtils.fromTicks(delay),
                                interval == null ? null : TimeUtils.fromTicks(interval));
        }

        public static Parameters after(Duration delay) {
            return fromDuration(checkNotNull(delay), null);
        }

        public static Parameters every(Duration interval) {
            return fromDuration(null, checkNotNull(interval));
        }

        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof Parameters)) return false;
            if(obj == this) return true;

            final Parameters parameters = (Parameters) obj;
            return Objects.equals(start(), parameters.start()) &&
                   Objects.equals(interval(), parameters.interval());
        }

        @Override
        public int hashCode() {
            return Objects.hash(start(), interval());
        }
    }
}
