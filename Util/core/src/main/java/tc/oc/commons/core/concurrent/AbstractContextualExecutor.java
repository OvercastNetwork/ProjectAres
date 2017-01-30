package tc.oc.commons.core.concurrent;

import java.util.concurrent.Executor;
import java.util.function.Consumer;
import javax.annotation.Nullable;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.commons.core.util.Pair;
import tc.oc.commons.core.util.ThrowingBiConsumer;

/**
 * Base implementation for {@link ContextualExecutor}.
 *
 * Just implement {@link #context()}.
 */
public abstract class AbstractContextualExecutor<C> implements ContextualExecutor<C> {

    private final Flexecutor executor;

    protected AbstractContextualExecutor(Executor executor) {
        this(Flexecutor.from(executor));
    }

    protected AbstractContextualExecutor(Flexecutor executor) {
        this.executor = executor;
    }

    /**
     * Called immediately before executing a task to retrieve the {@link C}
     * instance to pass to the task. If null is returned, the task is not run.
     */
    protected abstract @Nullable C context();

    @Override
    public void execute(Consumer<C> task) {
        executor.execute(() -> {
            final C context = context();
            if(context != null) task.accept(context);
        });
    }

    @Override
    public <T> void callback(ListenableFuture<T> future, FutureCallback<Pair<C, T>> callback) {
        executor.callback(future, new FutureCallback<T>() {
            @Override public void onSuccess(@Nullable T result) {
                final C context = context();
                if(context != null) callback.onSuccess(Pair.create(context, result));
            }

            @Override public void onFailure(Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    @Override
    public <T> void callback(ListenableFuture<T> future, ThrowingBiConsumer<C, T, Exception> callback) {
        executor.callback(future, result -> {
            final C context = context();
            if(context != null) callback.accept(context, result);
        });
    }
}
