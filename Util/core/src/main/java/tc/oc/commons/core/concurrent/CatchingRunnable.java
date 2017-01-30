package tc.oc.commons.core.concurrent;

import javax.annotation.Nullable;

import tc.oc.commons.core.exception.ExceptionHandler;
import tc.oc.commons.core.util.StackTrace;
import tc.oc.commons.core.util.ThrowingRunnable;
import tc.oc.commons.core.util.Traceable;

public class CatchingRunnable<T extends Throwable> implements Runnable, Traceable {

    private final ExceptionHandler handler;
    private final Runnable runnable;
    private final @Nullable StackTrace trace;

    public CatchingRunnable(ExceptionHandler<?> handler, Runnable runnable, @Nullable StackTrace trace) {
        this.handler = handler;
        this.runnable = runnable;
        this.trace = trace;
    }

    public CatchingRunnable(ExceptionHandler<?> handler, ThrowingRunnable<? extends T> runnable, @Nullable StackTrace trace) {
        this(handler, (Runnable) runnable, trace);
    }

    @Override
    public @Nullable StackTrace stackTrace() {
        return trace;
    }

    @Override
    public void run() {
        try {
            if(runnable instanceof ThrowingRunnable) {
                ((ThrowingRunnable) runnable).runThrows();
            } else {
                runnable.run();
            }
        } catch(Throwable throwable) {
            handler.handleException(throwable, runnable, trace);
        }
    }
}
