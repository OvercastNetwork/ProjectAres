package tc.oc.commons.core.collection;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;

import com.google.common.collect.ForwardingMap;

/**
 * A {@link ForwardingMap} that delegates all insertion operations to {@link #putInternal},
 * even those that are done through a collection view.
 */
public abstract class FilteredMap<K, V> extends ForwardingMap<K, V> {

    private @Nullable EntrySet entrySet;

    protected abstract V putInternal(K key, V value);

    @Override
    public V put(K key, V value) {
        return putInternal(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        standardPutAll(map);
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return (entrySet == null ? entrySet = createEntrySet() : entrySet);
    }

    protected EntrySet createEntrySet() {
        return new EntrySet();
    }

    protected class EntrySet extends FilteredSet<Entry<K, V>> {
        @Override
        protected Set<Entry<K, V>> delegate() {
            return FilteredMap.this.delegate().entrySet();
        }

        @Override
        protected boolean addInternal(Entry<K, V> entry) {
            final V value = entry.getValue();
            return !Objects.equals(value, put(entry.getKey(), value));
        }
    }
}
