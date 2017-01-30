package tc.oc.commons.core.collection;

import java.util.Set;
import javax.annotation.Nullable;

import com.google.common.collect.BiMap;
import com.google.common.collect.ForwardingMap;

public abstract class ForwardingBiMap<K, V> extends ForwardingMap<K, V> implements BiMap<K, V> {

    @Override
    protected abstract BiMap<K, V> delegate();

    @Override
    public V forcePut(@Nullable K key, @Nullable V value) {
        return delegate().forcePut(key, value);
    }

    @Override
    public BiMap<V, K> inverse() {
        return delegate().inverse();
    }

    @Override
    public Set<V> values() {
        return delegate().values();
    }
}
