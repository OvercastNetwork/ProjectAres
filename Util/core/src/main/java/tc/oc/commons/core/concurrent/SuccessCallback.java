package tc.oc.commons.core.concurrent;

import java.util.function.Consumer;
import javax.annotation.Nullable;

import com.google.common.util.concurrent.FutureCallback;

public class SuccessCallback<T> implements FutureCallback<T> {

    private final Consumer<T> consumer;

    public SuccessCallback(Consumer<T> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void onSuccess(@Nullable T result) {
        consumer.accept(result);
    }

    @Override
    public void onFailure(Throwable t) {}
}
