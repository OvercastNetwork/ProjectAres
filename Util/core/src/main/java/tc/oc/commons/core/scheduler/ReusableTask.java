package tc.oc.commons.core.scheduler;

/**
 * A {@link Task} that can be rescheduled at any time, even after
 * being cancelled. Each instance can only have one schedule at
 * a time. Setting new parameters effectively cancels any previous
 * schedule. This is useful for debouncing.
 */
public class ReusableTask extends AbstractTask {

    private Parameters parameters;

    ReusableTask(Scheduler scheduler, Runnable runnable) {
        super(scheduler, runnable);
    }

    @Override
    public Parameters getParameters() {
        if(parameters == null) {
            throw new IllegalStateException("Task is not scheduled");
        }
        return parameters;
    }

    public void schedule(Parameters parameters) {
        cancel();
        this.parameters = parameters;
        scheduler.startTask(this);
    }

    @Override
    public void cancel() {
        if(parameters != null) {
            scheduler.cancelTask(this);
        }
    }

    @Override
    void setCancelled() {
        super.setCancelled();
        this.parameters = null;
    }
}
