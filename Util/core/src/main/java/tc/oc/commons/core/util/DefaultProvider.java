package tc.oc.commons.core.util;

public interface DefaultProvider<K, V> {
    public V get(K key);
}
