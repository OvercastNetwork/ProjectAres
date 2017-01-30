package tc.oc.commons.core.concurrent;

import java.util.concurrent.Callable;
import javax.annotation.Nullable;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.SettableFuture;
import tc.oc.commons.core.util.StackTrace;
import tc.oc.commons.core.util.ThrowingRunnable;
import tc.oc.commons.core.util.Traceables;

public interface CatchingExecutorService extends CatchingExecutor, ListeningExecutorService {

    default <T> ListenableFuture<T> submit(Callable<T> task, @Nullable StackTrace source) {
        final SettableFuture<T> future = SettableFuture.create();
        executeCatch(() -> {
            final T result;
            try {
                result = task.call();
            } catch(Throwable ex) {
                future.setException(ex);
                throw ex;
            }
            future.set(result);
        }, source);
        return future;
    }

    @Override
    default <T> ListenableFuture<T> submit(Callable<T> task) {
        return submit(task, Traceables.computeStackTrace(task, CatchingExecutorService.class));
    }

    default <T> ListenableFuture<T> submit(ThrowingRunnable<?> task, T result, @Nullable StackTrace source) {
        final SettableFuture<T> future = SettableFuture.create();
        executeCatch(() -> {
            try {
                task.runThrows();
            } catch(Throwable ex) {
                future.setException(ex);
                throw ex;
            }
            future.set(result);
        }, source);
        return future;
    }

    default <T> ListenableFuture<T> submit(ThrowingRunnable<?> task, T result) {
        return submit(task, result, Traceables.computeStackTrace(task, CatchingExecutorService.class));
    }

    default ListenableFuture<?> submit(ThrowingRunnable<?> task, @Nullable StackTrace source) {
        return submit(task, null, source);
    }

    default ListenableFuture<?> submit(ThrowingRunnable<?> task) {
        return submit(task, Traceables.computeStackTrace(task, CatchingExecutorService.class));
    }


    default <T> ListenableFuture<T> submit(Runnable task, T result, @Nullable StackTrace source) {
        return submit((ThrowingRunnable<?>) task::run, result, source);
    }

    @Override
    default <T> ListenableFuture<T> submit(Runnable task, T result) {
        return submit(task, result, Traceables.computeStackTrace(task, CatchingExecutorService.class));
    }

    default ListenableFuture<?> submit(Runnable task, @Nullable StackTrace source) {
        return submit((ThrowingRunnable<?>) task::run, source);
    }

    @Override
    default ListenableFuture<?> submit(Runnable task) {
        return submit(task, Traceables.computeStackTrace(task, CatchingExecutorService.class));
    }
}
