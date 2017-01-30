package tc.oc.commons.core.concurrent;

import java.util.function.Consumer;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.commons.core.util.Pair;
import tc.oc.commons.core.util.ThrowingBiConsumer;

/**
 * An {@link java.util.concurrent.Executor} that executes tasks contingent
 * on the availability of some contextual object at the time the task is run.
 * If the context object is available, it is passed to the consumer/callback.
 * If it is not available, the consumer/callback does not run.
 *
 * The methods that take a {@link ListenableFuture} parameter work the same
 * way as the respective methods in {@link Flexecutor} i.e. they
 * simply register the callback with the future to be called on this executor.
 */
public interface ContextualExecutor<C> {

    void execute(Consumer<C> task);

    <T> void callback(ListenableFuture<T> future, FutureCallback<Pair<C, T>> callback);

    <T> void callback(ListenableFuture<T> future, ThrowingBiConsumer<C, T, Exception> callback);
}
