package tc.oc.commons.core.scheduler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.collect.ImmutableSet;
import java.time.Duration;
import java.time.Instant;
import tc.oc.commons.core.concurrent.CatchingRunnable;
import tc.oc.commons.core.exception.ExceptionHandler;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.plugin.PluginScoped;
import tc.oc.commons.core.util.StackTrace;

/**
 * Represents a disposable {@link Task} scheduler.
 *
 * This mostly acts as a wrapper for the Bukkit/Bungee schedulers, however
 * it adds additional functionality such as debouncing, phased registration,
 * and universal cancellation.
 */
@PluginScoped
public class Scheduler {

    protected final Logger logger;
    private final SchedulerBackend<Object> backend;
    private final ExceptionHandler exceptionHandler;

    private final Set<AbstractTask> registered = new HashSet<>();
    private final Set<AbstractTask> started = new HashSet<>();

    private final Set<Class<?>> skipTraceClasses = ImmutableSet.of(getClass(), State.class, Unstarted.class, Cancelled.class, Running.class);

    private State state;

    @Inject protected Scheduler(Loggers loggers, SchedulerBackend backend, ExceptionHandler exceptionHandler) {
        this(loggers, backend, exceptionHandler, true);
    }

    public Scheduler(Loggers loggers, SchedulerBackend backend, ExceptionHandler exceptionHandler, boolean started) {
        this.exceptionHandler = exceptionHandler;
        this.logger = loggers.get(getClass());
        this.backend = backend;

        state = started ? new Running() : new Unstarted();
    }

    protected Task register(Task.Parameters parameters, Runnable runnable) {
        return register(parameters, runnable, new StackTrace(skipTraceClasses));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{registered=" + registered + ", started=" + started + ", state=" + state.getClass().getSimpleName() + "}";
    }

    public Task createTask(Runnable task) {
        return register(Task.Parameters.fromDuration(null, null), task);
    }

    public Task createDelayedTask(Duration delay, Runnable task) {
        return register(Task.Parameters.fromDuration(delay, null), task);
    }

    public Task createDelayedTask(long delay, Runnable task) {
        return register(Task.Parameters.fromTicks(delay, null), task);
    }

    public Task createDelayedTask(Instant when, Runnable task) {
        final Instant now = Instant.now();
        if(when.isAfter(Instant.now())) {
            return createDelayedTask(Duration.between(now, when), task);
        } else {
            return createTask(task);
        }
    }

    public Task createRepeatingTask(Duration interval, Runnable task) {
        return register(Task.Parameters.fromDuration(Duration.ZERO, interval), task);
    }

    public Task createRepeatingTask(long interval, Runnable task) {
        return register(Task.Parameters.fromTicks(0L, interval), task);
    }

    public Task createRepeatingTask(Duration delay, Duration interval, Runnable task) {
        return register(Task.Parameters.fromDuration(delay, interval), task);
    }

    public Task createRepeatingTask(long delay, long interval, Runnable task) {
        return register(Task.Parameters.fromTicks(delay, interval), task);
    }

    /**
     * Create a new {@link ReusableTask} with no schedule
     */
    public ReusableTask createReusableTask(Runnable runnable) {
        return new ReusableTask(this, new CatchingRunnable(exceptionHandler, runnable, new StackTrace(skipTraceClasses)));
    }

    public DebouncedTask createDebouncedTask(Duration delay, Runnable runnable) {
        return new DebouncedTask(this, delay, runnable);
    }

    public DebouncedTask createDebouncedTask(Runnable runnable) {
        return new DebouncedTask(this, runnable);
    }


    // Keep all synchronized methods below

    /**
     * Start all registered {@link Task}s from now on.
     */
    synchronized public void start() {
        state.start();
    }

    /**
     * Permanently disable this scheduler and cancel all {@link Task}s in the past and future.
     */
    synchronized public void cancel() {
        state.cancel();
    }

    synchronized protected Task register(Task.Parameters parameters, Runnable runnable, @Nullable StackTrace trace) {
        return state.register(parameters, new CatchingRunnable(exceptionHandler, runnable, trace));
    }

    synchronized protected boolean isTaskQueued(AbstractTask task) {
        return state.isTaskQueued(task);
    }

    synchronized protected boolean isTaskRunning(AbstractTask task) {
        return state.isTaskRunning(task);
    }

    synchronized protected void cancelTask(AbstractTask task) {
        state.cancelTask(task);
    }

    /**
     * Run the given task only if there is no instance of that task's class already scheduled or running.
     * @return The handle of the newly scheduled task, if it was scheduled, otherwise the handle of the existing task.
     */
    synchronized public Task debounceTask(Runnable runnable) {
        for(Task task : state.pendingTasks()) {
            if(task.isPending() && task.getRunnable().getClass().isInstance(runnable)) {
                return task;
            }
        }
        return createTask(runnable);
    }

    protected Object startTask(AbstractTask task) {
        final Runnable runnable;
        if(task.getParameters().isRepeating()) {
            runnable = task.getRunnable();
        } else {
            runnable = () -> {
                try {
                    task.getRunnable().run();
                } finally {
                    synchronized(Scheduler.this) {
                        started.remove(task);
                    }
                }
            };
        }
        final Object backendTask = backend.startTask(task.getParameters(), runnable);
        task.setRunning(backendTask);
        synchronized(this) {
            started.add(task);
        }
        return backendTask;
    }

    private abstract class State {
        void start() {};
        void cancel() {};
        void cancelTask(AbstractTask task) {}

        abstract Iterable<? extends Task> pendingTasks();
        abstract boolean isTaskQueued(AbstractTask task);
        abstract boolean isTaskRunning(AbstractTask task);

        DisposableTask register(Task.Parameters parameters, CatchingRunnable runnable) {
            return new DisposableTask(Scheduler.this, parameters, runnable);
        }
    }

    private class Unstarted extends State {
        @Override
        void start() {
            registered.forEach(Scheduler.this::startTask);
            registered.clear();
            state = new Running();
        }

        @Override
        void cancel() {
            registered.clear();
            state = new Cancelled();
        }

        @Override
        DisposableTask register(Task.Parameters parameters, CatchingRunnable runnable) {
            final DisposableTask task = super.register(parameters, runnable);
            registered.add(task);
            return task;
        }

        @Override
        void cancelTask(AbstractTask task) {
            registered.remove(task);
        }

        @Override
        boolean isTaskQueued(AbstractTask task) {
            return registered.contains(task);
        }

        @Override
        boolean isTaskRunning(AbstractTask task) {
            return false;
        }

        @Override
        public Iterable<? extends Task> pendingTasks() {
            return registered;
        }
    }

    private class Running extends State {
        @Override
        void cancel() {
            ImmutableSet.copyOf(started).forEach(Task::cancel);
            started.clear();
            state = new Cancelled();
        }

        @Override
        DisposableTask register(Task.Parameters parameters, CatchingRunnable runnable) {
            final DisposableTask task = super.register(parameters, runnable);
            startTask(task);
            return task;
        }

        @Override
        boolean isTaskQueued(AbstractTask task) {
            return task.backend != null && backend.isTaskQueued(task.backend);
        }

        @Override
        boolean isTaskRunning(AbstractTask task) {
            return task.backend != null && backend.isTaskRunning(task.backend);
        }

        @Override
        void cancelTask(AbstractTask task) {
            started.remove(task);
            if(task.backend != null) {
                backend.cancelTask(task.backend);
            }
            task.setCancelled();
        }

        @Override
        public Iterable<? extends Task> pendingTasks() {
            return started
                .stream()
                .filter(Task::isPending)
                .collect(Collectors.toSet());
        }
    }

    private class Cancelled extends State {
        @Override
        void start() {
            throw new IllegalStateException("Scheduler has already been cancelled");
        }

        @Override
        DisposableTask register(Task.Parameters parameters, CatchingRunnable runnable) {
            final DisposableTask task = super.register(parameters, runnable);
            task.setCancelled();
            return task;
        }

        @Override
        boolean isTaskQueued(AbstractTask task) {
            return false;
        }

        @Override
        boolean isTaskRunning(AbstractTask task) {
            return false;
        }

        @Override
        public Iterable<Task> pendingTasks() {
            return Collections.emptySet();
        }
    }
}
