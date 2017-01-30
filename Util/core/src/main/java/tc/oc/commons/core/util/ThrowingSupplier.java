package tc.oc.commons.core.util;

import java.util.function.Supplier;

import com.google.common.base.Throwables;

@FunctionalInterface
public interface ThrowingSupplier<T, E extends Throwable> extends Supplier<T> {

    T getThrows() throws E;

    @Override
    default T get() {
        try {
            return getThrows();
        } catch(Throwable throwable) {
            throw Throwables.propagate(throwable);
        }
    }
}
