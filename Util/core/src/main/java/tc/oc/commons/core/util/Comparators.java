package tc.oc.commons.core.util;

import java.util.Comparator;
import java.util.function.Predicate;

public final class Comparators {
    private Comparators() {}

    private static final Comparator<Boolean> FALSE_FIRST = (a, b) -> a == b ? 0 : b ? -1 : 1;
    public static Comparator<Boolean> lastIf() {
        return FALSE_FIRST;
    }

    private static final Comparator<Boolean> TRUE_FIRST = (a, b) -> a == b ? 0 : a ? -1 : 1;
    public static Comparator<Boolean> firstIf() {
        return TRUE_FIRST;
    }

    public static <T> Comparator<T> lastIf(Predicate<T> predicate) {
        return (a, b) -> {
            final boolean pa = predicate.test(a), pb = predicate.test(b);
            return pa == pb ? 0 : pb ? -1 : 1;
        };
    }

    public static <T> Comparator<T> firstIf(Predicate<T> predicate) {
        return (a, b) -> {
            final boolean pa = predicate.test(a), pb = predicate.test(b);
            return pa == pb ? 0 : pa ? -1 : 1;
        };
    }

    public static <T> Comparator<T> firstIf(Predicate<T> predicate, Comparator<T> firstOrder, Comparator<T> lastOrder) {
        return (a, b) -> {
            final boolean pa = predicate.test(a), pb = predicate.test(b);
            if(pa == pb) {
                return (pa ? firstOrder : lastOrder).compare(a, b);
            } else {
                return pb ? -1 : 1;
            }
        };
    }

    public static <T> Comparator<T> lastIf(Predicate<T> predicate, Comparator<T> firstOrder, Comparator<T> lastOrder) {
        return firstIf(predicate.negate(), firstOrder, lastOrder);
    }

    public static <T> Comparator<T> instancesFirst(Class<?> type) {
        return firstIf(type::isInstance);
    }

    public static Comparator<?> instancesLast(Class<?> type) {
        return lastIf(type::isInstance);
    }

    public static <T, U> Comparator<T> instancesFirst(Class<U> type, Comparator<U> c) {
        return (a, b) -> {
            if(type.isInstance(a)) {
                if(type.isInstance(b)) {
                    return c.compare(type.cast(a), type.cast(b));
                } else {
                    return -1;
                }
            } else {
                return type.isInstance(b) ? 1 : 0;
            }
        };
    }

    public static <T, U> Comparator<T> instancesLast(Class<U> type, Comparator<U> c) {
        return (a, b) -> {
            if(type.isInstance(a)) {
                if(type.isInstance(b)) {
                    return c.compare(type.cast(a), type.cast(b));
                } else {
                    return -1;
                }
            } else {
                return type.isInstance(b) ? 1 : 0;
            }
        };
    }
}
