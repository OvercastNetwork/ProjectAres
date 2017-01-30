package tc.oc.commons.core.concurrent;

import java.sql.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nullable;

import com.google.common.util.concurrent.AbstractFuture;
import java.time.Duration;
import java.time.Instant;

/**
 * Abstract base for a {@link com.google.common.util.concurrent.ListenableFuture} that
 * fails automatically if it has not completed after a certain amount of time.
 *
 * This class implements the timeout, which starts when the object is instantiated.
 * When the timeout elapses, {@link #setException} will be called with a
 * {@link TimeoutException}. If this call succeeds, {@link #timeoutTask()} will be
 * called, which calls {@link #interruptTask()}.
 *
 * The timer is cancelled if the future completes in any way, or is cancelled.
 */
public abstract class TimeoutFuture<T> extends AbstractFuture<T> {

    private final TimerTask timeoutTask;
    private final long startNanos;
    private long stopNanos = -1;

    protected TimeoutFuture(Duration timeout) {
        this(Instant.now().plus(timeout));
    }

    protected TimeoutFuture(Instant expiresAt) {
        this.startNanos = System.nanoTime();

        this.timeoutTask = new TimerTask() {
            @Override public void run() {
                if(setException(new TimeoutException(makeTimeoutMessage()))) {
                    timeoutTask();
                }
            }
        };

        makeTimer().schedule(timeoutTask, Date.from(expiresAt));
    }

    protected String makeTimeoutMessage() {
        return toString() + " timed out";
    }

    protected String makeTimerName() {
        return toString() + " timer thread";
    }

    protected Timer makeTimer() {
        return new Timer(makeTimerName(), true);
    }

    protected void timeoutTask() {
        interruptTask();
    }

    private void stop() {
        timeoutTask.cancel();
        if(stopNanos == -1) {
            stopNanos = System.nanoTime();
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if(super.cancel(mayInterruptIfRunning)) {
            stop();
            return true;
        }
        return false;
    }

    @Override
    protected boolean set(@Nullable T value) {
        stop();
        return super.set(value);
    }

    @Override
    protected boolean setException(Throwable throwable) {
        stop();
        return super.setException(throwable);
    }

    /**
     * If the task is still running, return the elapsed time since it started.
     * If the task is completed or cancelled, return the time it was running for.
     */
    public long elapsedTimeNanos() {
        if(stopNanos == -1) {
            return System.nanoTime() - startNanos;
        } else {
            return stopNanos - startNanos;
        }
    }

    public double elapsedTimeMillis() {
        return elapsedTimeNanos() / 1000000d;
    }

    public Duration elapsedTime() {
        return Duration.ofMillis(Math.round(elapsedTimeMillis()));
    }
}
