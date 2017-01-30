package tc.oc.commons.core.util;

/**
 * Extend {@link Comparable} with some more fluent comparison methods.
 *
 * The term "equal" is avoided since {@link #compareTo} is not strictly required
 * to be consistent with {@link #equals}.
 */
public interface Orderable<T> extends Comparable<T> {
    default boolean greaterThan(T that) {
        return compareTo(that) > 0;
    }

    default boolean noLessThan(T that) {
        return compareTo(that) >= 0;
    }

    default boolean lessThan(T that) {
        return compareTo(that) < 0;
    }

    default boolean noGreaterThan(T that) {
        return compareTo(that) <= 0;
    }

    default boolean noGreaterOrLessThan(T that) {
        return compareTo(that) == 0;
    }
}
