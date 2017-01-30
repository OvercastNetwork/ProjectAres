package tc.oc.commons.core.concurrent;

import java.util.function.Consumer;
import javax.annotation.Nullable;

import com.google.common.util.concurrent.FutureCallback;

public class FailureCallback<T> implements FutureCallback<T> {

    private final Consumer<Throwable> consumer;

    public FailureCallback(Consumer<Throwable> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void onSuccess(@Nullable T result) {}

    @Override
    public void onFailure(Throwable ex) {
        consumer.accept(ex);
    }
}
