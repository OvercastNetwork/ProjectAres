package tc.oc.commons.core.collection;

import java.util.function.UnaryOperator;

import com.google.common.collect.BiMap;

/**
 * A {@link BiMap} that transforms keys on insertion to make them unique. It does this by
 * repeatedly applying the given {@link UnaryOperator} to any newly added key until it is
 * distinct from any key already in the map. A hard limit must be specified on the number
 * of iterations before giving up and throwing an {@link IllegalArgumentException}.
 *
 * This class extends {@link BiMap} since reverse lookups are likely needed in many use cases.
 *
 * The conflict resolution can be bypassed by calling {@link #forcePut}, which will silently
 * replace any existing entry with the same key (or with the same value, as per the
 * specification in {@link BiMap}).
 */
public abstract class ConflictResolvingMap<K, V> extends FilteredBiMap<K, V> {

    private final UnaryOperator<K> uniquifier;
    private final int limit;

    public ConflictResolvingMap(int limit, UnaryOperator<K> uniquifier) {
        this.uniquifier = uniquifier;
        this.limit = limit;
    }

    /**
     * Resolve the actual key that would be used if the given key and value
     * were inserted into the map in its current state, but do not actually
     * make any changes to the map.
     */
    public K resolveKey(K key, V value) {
        final BiMap<K, V> delegate = delegate();
        for(int tries = 0; tries < limit; tries++) {
            final V existing = delegate.get(key);
            if(existing == null || existing.equals(value)) {
                return key;
            }
            key = uniquifier.apply(key);
        }
        throw new IllegalArgumentException("Failed to generate a unique key after " + limit + " attempts");
    }

    /**
     * Equivalent to {@link #put}, but the resolved key is returned instead
     * of the old value (which is always null anyway).
     */
    public K putReturningKey(K key, V value) {
        key = resolveKey(key, value);
        delegate().put(key, value);
        return key;
    }

    @Override
    protected V putInternal(K key, V value) {
        key = resolveKey(key, value);
        return delegate().put(key, value);
    }

    @Override
    protected V forcePutInternal(K key, V value) {
        return delegate().forcePut(key, value);
    }
}
