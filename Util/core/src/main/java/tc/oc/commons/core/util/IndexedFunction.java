package tc.oc.commons.core.util;

@FunctionalInterface
public interface IndexedFunction<T, R> {
    R apply(T t, int index);
}
