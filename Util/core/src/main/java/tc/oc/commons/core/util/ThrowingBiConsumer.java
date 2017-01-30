package tc.oc.commons.core.util;

import java.util.function.BiConsumer;

import com.google.common.base.Throwables;

/**
 * @see ThrowingConsumer
 */
@FunctionalInterface
public interface ThrowingBiConsumer<T, U, E extends Throwable> extends BiConsumer<T, U> {

    void acceptThrows(T t, U u) throws E;

    @Override
    default void accept(T t, U u) {
        try {
            acceptThrows(t, u);
        } catch(Throwable throwable) {
            throw Throwables.propagate(throwable);
        }
    }
}
