package tc.oc.commons.core.util;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;

public class Utils {

    public static boolean notEqual(Object a, Object b) {
        return !Objects.equals(a, b);
    }

    public static <T> boolean equals(Class<T> type, T self, Object that, Predicate<T> test) {
        return self == that || (type.isInstance(that) && test.test(type.cast(that)));
    }

    public static boolean isInstanceOf(Optional<?> value, Class<?> type) {
        return value.isPresent() && type.isInstance(value.get());
    }

    public static <T> Optional<T> getInstanceOf(Optional<? super T> value, Class<T> type) {
        return isInstanceOf(value, type) ? (Optional<T>) value : Optional.empty();
    }

    public static <T> Optional<T> getInstanceOf(@Nullable Object obj, Class<T> type) {
        return type.isInstance(obj) ? Optional.ofNullable((T) obj)
                                    : Optional.empty();
    }

    public static boolean contains(Optional<?> container, Object value) {
        return container.isPresent() && container.get().equals(value);
    }

    public static <S> void ifInstance(Object generic, Class<S> specific, Consumer<S> action) {
        if(specific.isInstance(generic)) {
            action.accept((S) generic);
        }
    }
}
