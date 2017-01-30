package tc.oc.commons.core.util;

@FunctionalInterface
public interface IndexedConsumer<T> {
    void accept(T t, int index);
}
