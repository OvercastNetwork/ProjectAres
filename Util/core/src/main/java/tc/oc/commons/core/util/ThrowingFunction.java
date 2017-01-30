package tc.oc.commons.core.util;

import java.util.function.Function;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.UncheckedExecutionException;

/**
 * A {@link Function} that can throw anything. Call {@link #applyThrows} directly
 * if you want to handle the exceptions, or call {@link #apply} to have them
 * wrapped in a {@link UncheckedExecutionException}.
 *
 * TODO: Catches everything, not just {@link E}.. not ideal
 */
@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Throwable> extends Function<T, R> {

    R applyThrows(T t) throws E;

    @Override
    default R apply(T t) {
        try {
            return applyThrows(t);
        } catch(Throwable throwable) {
            throw Throwables.propagate(throwable);
        }
    }
}
