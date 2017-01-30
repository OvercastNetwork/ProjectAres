package tc.oc.commons.core.util;

import java.util.function.Predicate;

public class Predicates {
    private static final Predicate<?> ALWAYS_TRUE = x -> true;
    public static <T> Predicate<T> alwaysTrue() { return (Predicate<T>) ALWAYS_TRUE; }

    private static final Predicate<?> ALWAYS_FALSE = x -> false;
    public static <T> Predicate<T> alwaysFalse() { return (Predicate<T>) ALWAYS_FALSE; }

    public static <T> Predicate<T> not(Predicate<T> pred) { return t -> !pred.test(t); }
}
