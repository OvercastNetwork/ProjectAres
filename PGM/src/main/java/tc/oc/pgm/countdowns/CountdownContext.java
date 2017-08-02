package tc.oc.pgm.countdowns;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.inject.Inject;

import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerSuspendEvent;
import tc.oc.commons.core.IterableUtils;
import tc.oc.commons.core.concurrent.SerializingExecutor;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.scheduler.Scheduler;
import tc.oc.commons.core.scheduler.Task;
import tc.oc.commons.core.util.Comparables;
import tc.oc.commons.core.util.Predicates;
import tc.oc.commons.core.util.Streams;
import tc.oc.commons.core.util.TimeUtils;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.time.TickClock;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@ListenerScope(MatchScope.LOADED)
public abstract class CountdownContext implements Listener {

    public static final Duration MIN_REPEAT_INTERVAL = Duration.ofMillis(50);

    protected Logger logger;
    @Inject void setLogger(Loggers loggers) { logger = loggers.get(getClass()); }

    @Inject protected Scheduler scheduler;
    @Inject protected TickClock clock;

    /**
     * Must allow concurrent modification
     */
    protected abstract Stream<Runner> runners();

    protected abstract Optional<Runner> runner(Countdown countdown);

    protected abstract void addRunner(Runner runner);

    protected abstract void removeRunner(Runner runner);

    public abstract Set<Countdown> getAll();

    public void start(Countdown countdown, int seconds) {
        start(countdown, Duration.ofSeconds(seconds));
    }

    public void start(Countdown countdown, Duration duration) {
        start(countdown, duration, duration);
    }

    public void start(Countdown countdown, Duration initialDuration, @Nullable Duration repeatDuration) {
        start(countdown, initialDuration, repeatDuration, 1);
    }

    /**
     * Start running the given {@link Countdown} in this context. If the given Countdown
     * is already running, it will be cancelled and restarted with the given duration.
     */
    public void start(Countdown countdown, Duration initialDuration, @Nullable Duration repeatDuration, int repeatCount) {
        cancel(countdown);
        new Runner(countdown, initialDuration, repeatDuration != null ? repeatDuration : initialDuration, repeatCount);
    }

    public void cancel(Countdown countdown) {
        runner(countdown).ifPresent(r -> r.cancel(false));
    }

    public Stream<Countdown> countdowns() {
        return getAll().stream();
    }

    public <T extends Countdown> Stream<T> countdowns(Class<T> type) {
        return Streams.instancesOf(countdowns(), type);
    }

    public <T extends Countdown> Set<T> getAll(Class<T> type) {
        return IterableUtils.instancesOf(getAll(), type);
    }

    public @Nullable Duration getTimeLeft(Countdown countdown) {
        return runner(countdown).map(runner -> Duration.ofSeconds(runner.secondsRemaining))
                                .orElse(null);
    }

    public boolean isRunning(Countdown countdown) {
        return runner(countdown).filter(runner -> runner.secondsRemaining > 0)
                                .isPresent();
    }

    public boolean anyRunning() {
        return anyRunning(Predicates.alwaysTrue());
    }

    public boolean anyRunning(Predicate<? super Countdown> test) {
        return countdowns().anyMatch(test);
    }

    public boolean anyRunning(Class<? extends Countdown> countdownClass) {
        return anyRunning(countdown -> countdown.getClass().equals(countdownClass));
    }

    public void cancelAll() {
        cancelAll(false);
    }

    public void cancelAll(boolean manual) {
        if(anyRunning()) {
            logger.fine("Cancelling all countdowns");
            runners().forEach(runner -> runner.cancel(manual));
        }
    }

    /**
     * Cancel all countdowns of the given type
     * @return true if any countdowns were cancelled
     */
    public boolean cancelAll(Class<? extends Countdown> type) {
        if(anyRunning(type::isInstance)) {
            logger.fine("Cancelling all " + type.getSimpleName() + " countdowns");
            runners().filter(r -> type.isInstance(r.countdown))
                     .forEach(r -> r.cancel(false));
            return true;
        }
        return false;
    }

    /**
     * Cancel all countdowns that are not of the given type
     * @return true if any countdowns were cancelled
     */
    public boolean continueAll(Class<? extends Countdown> type) {
        if(anyRunning(c -> !type.isInstance(c))) {
            logger.fine("Cancelling all countdowns except " + type.getSimpleName());
            runners().filter(r -> !type.isInstance(r.countdown))
                     .forEach(r -> r.cancel(false));
            return true;
        }
        return false;
    }

    @EventHandler
    void suspend(ServerSuspendEvent event) throws EventException {
        try { event.yield(); }
        finally {
            runners().forEach(Runner::resume);
        }
    }

    protected class Runner implements Runnable {

        protected final Countdown countdown;
        protected final Duration initialDuration;
        protected final Duration repeatDuration;
        private final int repeatCount;

        private int count;
        private Duration duration;
        private Instant startedAt;
        private Instant willEndAt;

        // The remaining seconds that will be passed to onTick for the next cycle
        private long secondsRemaining;

        // Update task, replaced on every update
        private @Nullable Task task = null;

        // Ensures that only one callback is executing at a time for this countdown.
        // So, e.g. if the onStart callback cancels the countdown, onCancel won't
        // run until onStart returns.
        private final SerializingExecutor notifyExecutor = new SerializingExecutor();

        Runner(Countdown countdown, Duration initialDuration, Duration repeatDuration, int repeatCount) {
            checkArgument(!(repeatCount > 1 && Comparables.lessThan(repeatDuration, MIN_REPEAT_INTERVAL)));

            this.countdown = checkNotNull(countdown);
            this.initialDuration = checkNotNull(initialDuration);
            this.repeatDuration = checkNotNull(repeatDuration);
            this.repeatCount = repeatCount;

            restart();
        }

        void restart() {
            ++count;
            if(repeatCount == Integer.MAX_VALUE || count <= repeatCount) {
                duration = count == 1 ? initialDuration : repeatDuration;
                startedAt = clock.now().instant;

                if(TimeUtils.isInfPositive(duration)) {
                    willEndAt = TimeUtils.INF_FUTURE;
                    secondsRemaining = Long.MAX_VALUE;
                } else {
                    willEndAt = startedAt.plus(duration);
                    secondsRemaining = duration.getSeconds();
                    task = scheduler.createTask(this);
                }

                if(logger.isLoggable(Level.FINE)) {
                    logger.fine("Starting countdown " + countdown +
                                " at " + startedAt +
                                " for " + duration);
                }

                addRunner(this);

                notify(() -> countdown.onStart(duration, duration));
            }
        }

        protected Countdown countdown() {
            return countdown;
        }

        protected Duration remaining() {
            return TimeUtils.duration(clock.now().instant, willEndAt);
        }

        protected void cancel(boolean manual) {
            logger.fine("Cancelling countdown " + countdown);

            final Duration remaining = TimeUtils.duration(clock.now().instant, willEndAt);

            if(task != null) {
                task.cancel();
                task = null;
            }

            removeRunner(this);

            notify(() -> countdown.onCancel(remaining, duration, manual));
        }

        @Override
        public void run() {
            if(task == null) return;
            task = null;

            if(secondsRemaining < 0) return;

            // Get the total ticks remaining in the countdown
            long ticksRemaining = Math.round(remaining().toMillis() / 50d);

            // Handle any cycles since the last one
            for(;secondsRemaining >= 0 && secondsRemaining * 20 >= ticksRemaining; secondsRemaining--) {
                countdown.onTick(Duration.ofSeconds(secondsRemaining), duration);
            }

            if(secondsRemaining >= 0) {
                // If there are cycles left, schedule the next run
                long ticks = ticksRemaining - secondsRemaining * 20;
                task = scheduler.createDelayedTask(ticks < 1 ? 1 : ticks, this);
            } else {
                // Otherwise, end the countdown
                logger.fine("Ending countdown " + countdown);

                secondsRemaining = 0;

                // Remove from context before calling onEnd, so if it starts another
                // countdown, it won't try to cancel this one.
                if(count >= repeatCount) {
                    removeRunner(this);
                }

                notify(() -> countdown.onEnd(duration));

                restart();
            }
        }

        void notify(Runnable callback) {
            try {
                notifyExecutor.execute(callback);
            } catch(Throwable e) {
                logger.log(Level.SEVERE, "Exception notifying countdown " + countdown, e);
            }
        }

        void resume() {
            // Skip all the ticks that happened while suspended
            secondsRemaining = remaining().getSeconds();
        }
    }
}
