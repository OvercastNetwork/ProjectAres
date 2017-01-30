package tc.oc.commons.core.util;

import javax.annotation.Nullable;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

/**
 * A {@link FutureCallback} adapter that provides a {@link ListenableFuture} for its own completion.
 * The future completes after the delegate callback returns.
 */
public class CompletableFutureCallback<T> implements FutureCallback<T> {

    private final FutureCallback<T> delegate;
    private final SettableFuture<T> future = SettableFuture.create();

    public CompletableFutureCallback(FutureCallback<T> delegate) {
        this.delegate = delegate;
    }

    public ListenableFuture<T> future() {
        return future;
    }

    @Override
    public void onSuccess(@Nullable T result) {
        try {
            delegate.onSuccess(result);
            future.set(result);
        } catch(Throwable t) {
            future.setException(t);
            throw t;
        }
    }

    @Override
    public void onFailure(Throwable t) {
        try {
            delegate.onFailure(t);
        } finally {
            future.setException(t);
        }
    }
}
