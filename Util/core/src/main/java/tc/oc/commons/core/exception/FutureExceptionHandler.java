package tc.oc.commons.core.exception;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.util.concurrent.FutureCallback;

public class FutureExceptionHandler implements FutureCallback<Object> {

    private final ExceptionHandler exceptionHandler;

    @Inject FutureExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    @Override public void onSuccess(@Nullable Object result) {}

    @Override
    public void onFailure(Throwable t) {
        exceptionHandler.handleException(t);
    }
}
