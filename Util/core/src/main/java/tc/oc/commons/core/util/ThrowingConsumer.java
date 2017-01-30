package tc.oc.commons.core.util;

import java.util.function.Consumer;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.UncheckedExecutionException;

/**
 * A {@link Consumer} that can throw anything. Call {@link #acceptThrows directly
 * if you want to handle the exceptions, or call {@link #accept} to have them
 * wrapped in a {@link UncheckedExecutionException}.
 */
@FunctionalInterface
public interface ThrowingConsumer<T, E extends Throwable> extends Consumer<T> {

    void acceptThrows(T t) throws E;

    @Override
    default void accept(T t) {
        try {
            acceptThrows(t);
        } catch(Throwable throwable) {
            throw Throwables.propagate(throwable);
        }
    }
}
