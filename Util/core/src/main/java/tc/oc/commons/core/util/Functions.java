package tc.oc.commons.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public interface Functions {
    static <T, R, E extends Exception> ThrowingFunction<T, R, E> memoize(ThrowingFunction<T, R, E> function) {
        final Map<T, R> results = new HashMap<>();
        return t -> MapUtils.computeIfAbsent(results, t, function);
    }

    /**
     * Return a {@link UnaryOperator} that passes its operand to
     * the given {@link Consumer} and then returns it.
     */
    static <T> UnaryOperator<T> tap(Consumer<? super T> consumer) {
        return t -> {
            consumer.accept(t);
            return t;
        };
    }

    /**
     * Like {@link #tap} except the consumer is skipped if the operand is null
     */
    static <T> UnaryOperator<T> tapUnlessNull(Consumer<? super T> consumer) {
        return t -> {
            if(t != null) consumer.accept(t);
            return t;
        };
    }
}
