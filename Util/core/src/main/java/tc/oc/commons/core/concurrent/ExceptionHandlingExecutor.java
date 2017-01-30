package tc.oc.commons.core.concurrent;

import javax.annotation.Nullable;

import tc.oc.commons.core.exception.ExceptionHandler;
import tc.oc.commons.core.util.StackTrace;
import tc.oc.commons.core.util.ThrowingRunnable;

@FunctionalInterface
public interface ExceptionHandlingExecutor extends CatchingExecutor {

    ExceptionHandler exceptionHandler();

    default void executeThrows(ThrowingRunnable<?> command, @Nullable StackTrace source) throws Throwable {
        command.runThrows();
    }

    @Override
    default void executeCatch(ThrowingRunnable<?> command, @Nullable StackTrace source) {
        try {
            executeThrows(command, source);
        } catch(Throwable t) {
            exceptionHandler().handleException(t, command, source);
        }
    }
}
