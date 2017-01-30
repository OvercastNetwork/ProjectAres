package tc.oc.commons.core.util;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;

/**
 * Helpers for working with nullable values
 */
public final class Nullables {
    private Nullables() {}

    /**
     * Return value if not null, else return def
     */
    public static <T> T orElse(T value, T def) {
        return value != null ? value : def;
    }

    public static <T, E extends Throwable> T orElseThrow(T value, Supplier<E> exceptionSupplier) throws E {
        if(value != null) return value;
        throw exceptionSupplier.get();
    }

    /**
     * If input is not null, return the result of applying the function to it,
     * otherwise return null.
     */
    public static @Nullable <In, Out> Out transform(@Nullable In input, Function<In, Out> function) {
        return input == null ? null : function.apply(input);
    }

    /**
     * If input is not null, return the result of applying the function to it,
     * otherwise return null.
     */
    public static @Nullable <In, Out, Ex extends Exception> Out transform(@Nullable In input, ThrowingFunction<In, Out, Ex> function) throws Ex {
        return input == null ? null : function.apply(input);
    }

    /**
     * If obj is an instance of the given type, cast and return it,
     * otherwise return null
     */
    public static @Nullable <T> T castOrNull(Object obj, Class<T> type) {
        return type.isInstance(obj) ? type.cast(obj) : null;
    }

    /**
     * Return the first non-null argument.
     * @throws NullPointerException if all arguments are null
     */
    @SafeVarargs
    public static <T> T first(T... a) {
        for(T t : a) if(t != null) return t;
        throw new NullPointerException();
    }

    /**
     * Return the first non-null argument, or null if all arguments are null.
     */
    @SafeVarargs
    public static @Nullable <T> T firstOrNull(T... a) {
        for(T t : a) if(t != null) return t;
        return null;
    }

    public static @Nullable <T> T firstOrNull(Stream<T> stream) {
        return stream.filter(t -> t != null)
                     .findFirst()
                     .orElse(null);
    }
}
