package tc.oc.commons.core.scheduler;

/**
 * A {@link Task} that is scheduled once with immutable parameters,
 * and cannot be used after completing or being cancelled.
 */
public class DisposableTask extends AbstractTask {

    protected final Parameters parameters;
    protected boolean cancelled = false;

    DisposableTask(Scheduler scheduler, Parameters parameters, Runnable runnable) {
        super(scheduler, runnable);
        this.parameters = parameters;
    }

    @Override
    public Parameters getParameters() {
        return parameters;
    }

    @Override
    public void cancel() {
        if(!cancelled) {
            scheduler.cancelTask(this);
        }
    }

    void setCancelled() {
        super.setCancelled();
        cancelled = true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{runnable=" + runnable + ", parameters=" + parameters + "}";
    }
}
