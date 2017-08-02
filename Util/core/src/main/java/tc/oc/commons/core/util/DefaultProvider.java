package tc.oc.commons.core.util;

public interface DefaultProvider<K, V> {
    V get(K key);
}
