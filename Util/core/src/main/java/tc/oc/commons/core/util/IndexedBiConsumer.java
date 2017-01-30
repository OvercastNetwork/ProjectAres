package tc.oc.commons.core.util;

@FunctionalInterface
public interface IndexedBiConsumer<T, U> {
    void accept(T t, U u, int index);
}
