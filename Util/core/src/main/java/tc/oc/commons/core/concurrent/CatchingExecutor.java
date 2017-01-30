package tc.oc.commons.core.concurrent;

import java.util.concurrent.Executor;
import javax.annotation.Nullable;

import tc.oc.commons.core.util.StackTrace;
import tc.oc.commons.core.util.ThrowingRunnable;
import tc.oc.commons.core.util.Traceables;

@FunctionalInterface
public interface CatchingExecutor extends Executor {

    void executeCatch(ThrowingRunnable<?> command, @Nullable StackTrace source);

    default void executeCatch(ThrowingRunnable<?> command) {
        executeCatch(command, Traceables.computeStackTrace(command, CatchingExecutor.class));
    }

    default void execute(Runnable command, @Nullable StackTrace source) {
        executeCatch(command::run, source);
    }

    @Override
    default void execute(Runnable command) {
        execute(command, Traceables.computeStackTrace(command, CatchingExecutor.class));
    }
}
