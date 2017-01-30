package tc.oc.commons.core.scheduler;

/**
 * Platform-specific driver for {@link Scheduler}
 */
public interface SchedulerBackend<BackendTask> {

    /**
     * Get a unique ID for a task that was created with {@link #startTask}
     */
    int taskId(BackendTask backendTask);

    /**
     * Is the given task queued to run at some future time?
     */
    boolean isTaskQueued(BackendTask backendTask);

    /**
     * Is the given task running right now?
     */
    boolean isTaskRunning(BackendTask backendTask);

    /**
     * Create a {@link BackendTask} and start it immediately.
     * The given runnable is already wrapped with exception handling code
     * by {@link Scheduler}, so it will not throw any exceptions, as long
     * as the exception handling code doesn't throw any.
     */
    BackendTask startTask(Task.Parameters schedule, Runnable runnable);

    /**
     * Cancel the given {@link BackendTask}.
     */
    void cancelTask(BackendTask backendTask);
}
