package tc.oc.commons.core.scheduler;

import javax.annotation.Nullable;

public abstract class AbstractTask implements Task {

    protected final Scheduler scheduler;
    protected final Runnable runnable;
    protected @Nullable Object backend;

    public AbstractTask(Scheduler scheduler, Runnable runnable) {
        this.scheduler = scheduler;
        this.runnable = runnable;
    }

    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    public Runnable getRunnable() {
        return runnable;
    }

    void setCancelled() {
        this.backend = null;
    }

    void setRunning(Object backend) {
        this.backend = backend;
    }

    @Override
    public boolean isRunning() {
        return scheduler.isTaskRunning(this);
    }

    @Override
    public boolean isQueued() {
        return scheduler.isTaskQueued(this);
    }

    @Override
    public boolean isPending() {
        return isQueued() || isRunning();
    }
}
