package tc.oc.commons.core.collection;

import java.util.Set;
import javax.annotation.Nullable;

import com.google.common.collect.BiMap;

/**
 * A forwarding {@link BiMap} that delegates all insertion operations to either
 * {@link #putInternal} or {@link #forcePutInternal}, even those done through
 * collection views.
 */
public abstract class FilteredBiMap<K, V> extends FilteredMap<K, V> implements BiMap<K, V> {

    private @Nullable Inverse inverse;

    @Override
    protected abstract BiMap<K, V> delegate();

    protected abstract V forcePutInternal(K key, V value);

    @Override
    public V forcePut(@Nullable K key, @Nullable V value) {
        return forcePutInternal(key, value);
    }

    @Override
    public Set<V> values() {
        return delegate().values();
    }

    @Override
    public BiMap<V, K> inverse() {
        return (inverse == null ? inverse = createInverse() : inverse);
    }

    protected Inverse createInverse() {
        return new Inverse();
    }

    protected class Inverse extends FilteredMap<V, K> implements BiMap<V, K> {
        @Override
        protected BiMap<V, K> delegate() {
            return FilteredBiMap.this.delegate().inverse();
        }

        @Override
        protected K putInternal(V value, K key) {
            final K old = get(value);
            FilteredBiMap.this.put(key, value);
            return old;
        }

        @Override
        public K forcePut(@Nullable V value, @Nullable K key) {
            final K old = get(value);
            FilteredBiMap.this.forcePut(key, value);
            return old;
        }

        @Override
        public Set<K> values() {
            return delegate().values();
        }

        @Override
        public BiMap<K, V> inverse() {
            return FilteredBiMap.this;
        }
    }
}
