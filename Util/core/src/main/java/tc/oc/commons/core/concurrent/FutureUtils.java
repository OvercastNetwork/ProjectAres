package tc.oc.commons.core.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Uninterruptibles;
import tc.oc.commons.core.util.Pair;
import tc.oc.commons.core.util.ThrowingFunction;

public final class FutureUtils {
    private FutureUtils() {}

    /**
     * Get the result of the given {@link Future}, retrying if interrupted using
     * {@link Uninterruptibles#getUninterruptibly}, and wrap any checked exceptions
     * other than the one given using {@link Throwables#propagate(Throwable)}.
     *
     * Use this when you only need to handle a particular checked exception,
     * and want any others can be handled generically.
     */
    public static <T, E extends Throwable> T getUncheckedExcept(Future<T> future, Class<E> except) throws E {
        try {
            return Uninterruptibles.getUninterruptibly(future);
        } catch(ExecutionException e) {
            if(except.isInstance(e.getCause())) {
                throw except.cast(e.getCause());
            } else {
                throw Throwables.propagate(e);
            }
        }
    }

    public static <T> FutureCallback<T> successCallback(Consumer<T> consumer) {
        return new SuccessCallback<>(consumer);
    }

    public static <T> FutureCallback<T> failureCallback(Consumer<Throwable> consumer) {
        return new FailureCallback<>(consumer);
    }

    public static <T> void onSuccess(ListenableFuture<T> future, Consumer<T> callback) {
        Futures.addCallback(future, successCallback(callback));
    }

    public static <T> void onSuccess(ListenableFuture<T> future, Consumer<T> callback, @Nullable Executor executor) {
        if(executor != null) {
            Futures.addCallback(future, successCallback(callback), executor);
        } else {
            onSuccess(future, callback);
        }
    }

    public static <T> void onFailure(ListenableFuture<T> future, Consumer<Throwable> callback) {
        Futures.addCallback(future, failureCallback(callback));
    }

    public static <T> void onFailure(ListenableFuture<T> future, Consumer<Throwable> callback, @Nullable Executor executor) {
        if(executor != null) {
            Futures.addCallback(future, failureCallback(callback), executor);
        } else {
            onFailure(future, callback);
        }
    }

    public static <T> ListenableFuture<T> peek(ListenableFuture<T> future, Consumer<T> callback) {
        return peek(future, callback, MoreExecutors.sameThreadExecutor());
    }

    public static <T> ListenableFuture<T> peek(ListenableFuture<T> future, Consumer<T> callback, Executor executor) {
        return Futures.transform(future, (com.google.common.base.Function<T, T>) t -> {
            callback.accept(t);
            return t;
        }, executor);
    }

    /**
     * Equivalent to {@link Futures#transform}, but can be passed a lambda unambiguously
     */
    public static <T, R> ListenableFuture<R> mapSync(ListenableFuture<T> in, ThrowingFunction<T, R, ?> op) {
        return mapSync(in, op, MoreExecutors.sameThreadExecutor());
    }

    public static <T, R> ListenableFuture<R> mapSync(ListenableFuture<T> in, ThrowingFunction<T, R, ?> op, Executor executor) {
        return mapAsync(in, out -> {
            try {
                return Futures.immediateFuture(op.applyThrows(out));
            } catch(Throwable ex) {
                return Futures.immediateFailedFuture(ex);
            }
        }, executor);
    }

    public static <T, R> ListenableFuture<R> mapAsync(ListenableFuture<T> in, AsyncFunction<T, R> op) {
        return Futures.transform(in, op);
    }

    public static <T, R> ListenableFuture<R> mapAsync(ListenableFuture<T> in, AsyncFunction<T, R> op, Executor executor) {
        return Futures.transform(in, op, executor);
    }

    public static <A, B> ListenableFuture<Pair<A, B>> pair(ListenableFuture<A> a, ListenableFuture<B> b) {
        return mapSync(Futures.allAsList(a, b), list -> Pair.of((A) list.get(0), (B) list.get(1)));
    }
}
