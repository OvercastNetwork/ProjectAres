package tc.oc.commons.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.util.concurrent.FutureCallback;
import tc.oc.commons.core.exception.ExceptionHandler;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link FutureCallback} used for system operations that should not fail.
 * Delegates {@link #onSuccess} to {@link #onSuccessThrows} and allows it
 * to throw any exception. Errors are sent to the given logger at SEVERE level,
 * along with the source location where the SystemFutureCallback was created.
 * Can be called asynchronously, assuming the logger is thread-safe.
 */
public class SystemFutureCallback<T> implements FutureCallback<T> {

    private static @Inject ExceptionHandler exceptionHandler;

    protected final StackTrace creationSite;
    private final @Nullable ThrowingConsumer<? super T, ?> successHandler;
    private final ListMultimap<Class<? extends Throwable>, ThrowingConsumer> failureHandlers = ArrayListMultimap.create();
    private final List<ThrowingRunnable<?>> completionHandlers = new ArrayList<>();

    public static <T> SystemFutureCallback<T> onSuccess(ThrowingConsumer<? super T, ?> handler) {
        return new SystemFutureCallback<>(checkNotNull(handler));
    }

    /**
     * @deprecated use {@link #SystemFutureCallback(ThrowingConsumer)}
     */
    @Deprecated
    public SystemFutureCallback() {
        this(null);
    }

    protected SystemFutureCallback(@Nullable ThrowingConsumer<? super T, ?> successHandler) {
        this.successHandler = successHandler;
        this.creationSite = new StackTrace();
    }

    /**
     * Add a handler for exceptions extending the given type.
     *
     * Exceptions will not be logged internally if they match any failure handlers.
     */
    public <X extends Throwable> SystemFutureCallback<T> onFailure(Class<X> exType, ThrowingConsumer<? super X, Throwable> handler) {
        failureHandlers.put(exType, handler);
        return this;
    }

    public SystemFutureCallback<T> onCompletion(ThrowingRunnable<Throwable> handler) {
        completionHandlers.add(handler);
        return this;
    }

    /**
     * @deprecated use {@link #SystemFutureCallback(ThrowingConsumer)}
     */
    @Deprecated
    public void onSuccessThrows(T result) throws Throwable {}

    @Override
    public void onSuccess(T result) {
        try {
            if(successHandler != null) {
                successHandler.acceptThrows(result);
            } else {
                onSuccessThrows(result);
            }
        } catch(Throwable e) {
            handleFailure(e);
        }
        handleCompletion();
    }

    @Override
    public void onFailure(Throwable e) {
        handleFailure(e);
        handleCompletion();
    }

    protected void handleDefaultFailure(Throwable e) {
        exceptionHandler.handleException(e, this, creationSite);
    }

    private void handleFailure(Throwable e) {
        boolean handled = false;
        for(Map.Entry<Class<? extends Throwable>, ThrowingConsumer> handler : failureHandlers.entries()) {
            if(handler.getKey().isInstance(e)) {
                try {
                    handler.getValue().acceptThrows(e);
                    handled = true;
                } catch(Throwable e1) {
                    handleDefaultFailure(e1);
                }
            }
        }
        if(!handled) {
            handleDefaultFailure(e);
        }
    }

    private void handleCompletion() {
        for(ThrowingRunnable<?> handler : completionHandlers) {
            try {
                handler.runThrows();
            } catch(Throwable e) {
                handleFailure(e);
            }
        }
    }
}
