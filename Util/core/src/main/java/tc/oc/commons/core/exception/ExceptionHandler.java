package tc.oc.commons.core.exception;

import java.util.Optional;
import javax.annotation.Nullable;

import com.google.common.base.Throwables;
import tc.oc.commons.core.reflect.TypeParameterCache;
import tc.oc.commons.core.util.StackTrace;
import tc.oc.commons.core.util.ThrowingRunnable;
import tc.oc.commons.core.util.ThrowingSupplier;

/**
 * An object that handles exceptions of some specified type. Essentially, this is
 * an encapsulated catch block.
 *
 * TODO: Merge with {@link tc.oc.exception.ExceptionHandler}
 */
public interface ExceptionHandler<T extends Throwable> {

    TypeParameterCache<ExceptionHandler, Throwable> CACHE_T = new TypeParameterCache<>(ExceptionHandler.class, "T");

    default Class<T> exceptionType() {
        return (Class<T>) CACHE_T.resolveRaw(getClass());
    }

    /**
     * Handle the given exception in some way. If a source is given, it is some executable
     * object from which the exception was thrown. The stack trace from the source object
     * will be the origin of that object, which may be entirely different than the trace
     * from the exception.
     */
    void handleException(T throwable, @Nullable Object source, @Nullable StackTrace trace);

    default void handleException(T throwable, @Nullable Object source) {
        handleException(throwable, source, null);
    }

    default void handleException(T throwable) {
        handleException(throwable, null);
    }

    default void run(ThrowingRunnable<T> block) {
        try {
            block.runThrows();
        } catch(Throwable ex) {
            final Class<T> type = exceptionType();
            if(type.isInstance(ex)) {
                handleException(type.cast(ex), block);
            } else {
                throw Throwables.propagate(ex);
            }
        }
    }

    default <U> Optional<U> get(ThrowingSupplier<U, T> block) {
        try {
            return Optional.of(block.getThrows());
        } catch(Throwable ex) {
            final Class<T> type = exceptionType();
            if(type.isInstance(ex)) {
                handleException(type.cast(ex), block);
                return Optional.empty();
            } else {
                throw Throwables.propagate(ex);
            }
        }
    }

    default <U> Optional<U> flatGet(ThrowingSupplier<Optional<U>, T> block) {
        return get(block).orElse(Optional.empty());
    }
}
