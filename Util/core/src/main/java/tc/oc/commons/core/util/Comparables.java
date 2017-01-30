package tc.oc.commons.core.util;

public interface Comparables {
    static <T extends Comparable<T>> boolean lessThan(T l, T r) {
        return l.compareTo(r) < 0;
    }

    static <T extends Comparable<T>> boolean lessOrEqual(T l, T r) {
        return l.compareTo(r) <= 0;
    }

    static <T extends Comparable<T>> boolean greaterThan(T l, T r) {
        return l.compareTo(r) > 0;
    }

    static <T extends Comparable<T>> boolean greaterOrEqual(T l, T r) {
        return l.compareTo(r) >= 0;
    }

    static <T extends Comparable<T>> T min(T a, T b) {
        return a.compareTo(b) <= 0 ? a : b;
    }

    static <T extends Comparable<T>> T max(T a, T b) {
        return a.compareTo(b) >= 0 ? a : b;
    }
}
