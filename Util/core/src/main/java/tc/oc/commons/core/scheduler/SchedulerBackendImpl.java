package tc.oc.commons.core.scheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;

import java.time.Duration;
import tc.oc.commons.core.plugin.PluginScoped;

/**
 * Generic {@link SchedulerBackend} implementation using the JDK {@link ScheduledExecutorService}.
 */
@PluginScoped
public class SchedulerBackendImpl implements SchedulerBackend<SchedulerBackendImpl.BackendTask> {

    private final ScheduledExecutorService executor;
    private int lastId;

    @Inject protected SchedulerBackendImpl(ThreadFactory threadFactory) {
        this.executor = Executors.newSingleThreadScheduledExecutor(threadFactory);
    }

    protected Runnable decorateRunnable(Runnable runnable) {
        return runnable;
    }

    @Override
    public int taskId(BackendTask task) {
        return task.id;
    }

    @Override
    public boolean isTaskQueued(BackendTask backendTask) {
        return backendTask.isQueued();
    }

    @Override
    public boolean isTaskRunning(BackendTask backendTask) {
        return backendTask.running.get();
    }

    @Override
    public BackendTask startTask(Task.Parameters schedule, Runnable runnable) {
        return new BackendTask(schedule, decorateRunnable(runnable));
    }

    @Override
    public void cancelTask(BackendTask backendTask) {
        backendTask.future.cancel(false);
    }

    class BackendTask {
        final int id;
        final ScheduledFuture<?> future;
        final Task.Parameters schedule;
        final Runnable runnable;
        final AtomicBoolean running = new AtomicBoolean();

        BackendTask(Task.Parameters schedule, Runnable runnable) {
            this.schedule = schedule;
            this.runnable = runnable;
            this.id = ++lastId;

            final Duration delay = schedule.delay();
            final Duration interval = schedule.interval();

            if(interval == null) {
                future = executor.schedule(
                    this.runnable,
                    delay == null ? 0 : delay.toNanos(),
                    TimeUnit.NANOSECONDS
                );
            } else {
                future = executor.scheduleAtFixedRate(
                    this.runnable,
                    delay == null ? 0 : delay.toNanos(),
                    interval == null ? 0 : interval.toNanos(),
                    TimeUnit.NANOSECONDS
                );
            }
        }

        boolean isQueued() {
            return !future.isDone() && !running.get();
        }
    }
}
