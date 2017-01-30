package tc.oc.commons.core.concurrent;

import java.util.concurrent.Executor;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.commons.core.util.SystemFutureCallback;
import tc.oc.commons.core.util.ThrowingConsumer;

/**
 * A decorator for {@link Executor} that adds various convenience methods.
 */
public interface Flexecutor extends Executor {

    /**
     * Decorate the given {@link Executor}, or return it if it is already a {@link Flexecutor}.
     */
    static Flexecutor from(Executor executor) {
        if(executor instanceof Flexecutor) {
            return (Flexecutor) executor;
        } else {
            return executor::execute;
        }
    }
    /**
     * Register the given {@link FutureCallback} to be executed through this executor when the given future completes.
     */
    default <T> void callback(ListenableFuture<T> future, FutureCallback<T> callback) {
        Futures.addCallback(future, callback, this);
    }

    /**
     * Register the given {@link ThrowingConsumer} to be executed through this executor when the given future completes.
     *
     * The consumer is wrapped in a {@link SystemFutureCallback}, which handles exceptions from both the future and the consumer.
     */
    default <T> void callback(ListenableFuture<T> future, ThrowingConsumer<T, Exception> consumer) {
        callback(future, SystemFutureCallback.onSuccess(consumer));
    }

    default Runnable wrap(Runnable task) {
        return () -> execute(task);
    }
}
