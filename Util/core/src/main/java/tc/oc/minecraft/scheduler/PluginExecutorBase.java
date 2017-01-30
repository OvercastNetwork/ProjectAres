package tc.oc.minecraft.scheduler;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AbstractListeningExecutorService;
import tc.oc.commons.core.concurrent.CatchingExecutorService;
import tc.oc.commons.core.concurrent.ExceptionHandlingExecutor;
import tc.oc.commons.core.concurrent.Flexecutor;
import tc.oc.commons.core.exception.ExceptionHandler;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.scheduler.Scheduler;
import tc.oc.commons.core.util.StackTrace;
import tc.oc.commons.core.util.ThrowingRunnable;
import tc.oc.minecraft.api.plugin.Plugin;

public abstract class PluginExecutorBase extends AbstractListeningExecutorService implements Flexecutor, CatchingExecutorService, ExceptionHandlingExecutor {

    protected Logger logger;
    @Inject
    void init(Loggers loggers) {
        logger = loggers.get(getClass());
    }

    @Inject protected Plugin plugin;
    @Inject protected ExceptionHandler exceptionHandler;

    private boolean terminated;

    protected PluginExecutorBase() {}

    @Override
    public ExceptionHandler exceptionHandler() {
        return exceptionHandler;
    }

    protected abstract void executeInternal(Runnable command);

    /**
     * If called from the main thread, run the given command immediately, otherwise
     * schedule it to run on the main thread, through the default {@link Scheduler}.
     */
    @Override
    public void executeThrows(ThrowingRunnable<?> command, @Nullable StackTrace source) throws Throwable {
        if(!plugin.isEnabled()) {
            logger.warning("Skipping execution because plugin is disabled");
            return;
        }

        executeInternal(() -> {
            try {
                command.runThrows();
            } catch(Throwable throwable) {
                exceptionHandler().handleException(throwable, command, source);
            }
        });
    }

    @Override
    public void shutdown() {
        terminated = true;
    }

    @Override
    public List<Runnable> shutdownNow() {
        shutdown();
        return ImmutableList.of();
    }

    @Override
    public boolean isShutdown() {
        return terminated;
    }

    @Override
    public boolean isTerminated() {
        return terminated;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return true;
    }
}
