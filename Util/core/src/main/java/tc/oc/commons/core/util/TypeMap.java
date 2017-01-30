package tc.oc.commons.core.util;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.ForwardingSetMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

/**
 * A {@link SetMultimap} with type keys that supports lookups based on type bounds.
 *
 * Keys are {@link TypeToken}s for types that extend {@link K} i.e. {@code TypeToken<? extends K>}.
 * Wherever {@link Class}es are accepted as keys, they are converted to the equivalent {@link TypeToken}.
 *
 * Note that unlike most map collections, the key type parameter is not the type of the keys,
 * but rather a bound for the keys, which are types themselves.
 *
 * There is no required relationship between the types of keys and values.
 */
public class TypeMap<K, V> extends ForwardingSetMultimap<TypeToken<? extends K>, V> implements MultimapHelper<TypeToken<? extends K>, V> {

    public static <K, V> TypeMap<K, V> create() {
        return new TypeMap<>(HashMultimap.create());
    }

    public static <K, V> TypeMap<K, V> wrap(SetMultimap<TypeToken<? extends K>, V> map) {
        return new TypeMap<>(map);
    }

    private final SetMultimap<TypeToken<? extends K>, V> map;

    public TypeMap(SetMultimap<TypeToken<? extends K>, V> map) {
        this.map = map;
    }

    public TypeMap(Map<TypeToken<? extends K>, ? extends Collection<V>> map, Supplier<Set<V>> supplier) {
        this(Multimaps.newSetMultimap((Map) map, supplier::get));
    }

    @Override
    protected SetMultimap<TypeToken<? extends K>, V> delegate() {
        return map;
    }

    public boolean put(Class<? extends K> key, V value) {
        return super.put(TypeToken.of(key), value);
    }

    /**
     * Return all keys within the given bounds
     */
    public Set<TypeToken<? extends K>> keysAssignableTo(TypeToken<? extends K> bounds) {
        return Sets.filter(keySet(), bounds::isAssignableFrom);
    }

    /**
     * Return all keys that bound the given type
     */
    public Set<TypeToken<? extends K>> keysAssignableFrom(TypeToken<? extends K> type) {
        return Sets.filter(keySet(), bounds -> bounds.isAssignableFrom(type));
    }

    public Set<TypeToken<? extends K>> keysAssignableTo(Class<? extends K> bounds) {
        return keysAssignableTo(TypeToken.of(bounds));
    }

    public Set<TypeToken<? extends K>> keysAssignableFrom(Class<? extends K> type) {
        return keysAssignableFrom(TypeToken.of(type));
    }

    /**
     * Return all values assigned to keys within the given bounds
     */
    public Set<V> allAssignableTo(TypeToken<? extends K> bounds) {
        return new SupersetView(Iterables.transform(keysAssignableTo(bounds), this::get));
    }

    /**
     * Return all values assigned to keys that bound the given type
     */
    public Set<V> allAssignableFrom(TypeToken<? extends K> type) {
        return new SupersetView(Iterables.transform(keysAssignableFrom(type), this::get));
    }

    /**
     * Return all values assigned to keys within the given bounds
     */
    public Set<V> allAssignableTo(Class<? extends K> bounds) {
        return allAssignableTo(TypeToken.of(bounds));
    }

    /**
     * Return all values assigned to keys that bound the given type
     */
    public Set<V> allAssignableFrom(Class<? extends K> type) {
        return allAssignableFrom(TypeToken.of(type));
    }

    /**
     * Return a single value assigned to a key within the given bounds
     * @throws NoSuchElementException if no such value exists
     * @throws AmbiguousElementException if multiple such values exist
     */
    public V oneAssignableTo(TypeToken<? extends K> bounds) {
        try {
            return Iterables.getOnlyElement(allAssignableTo(bounds));
        } catch(IllegalArgumentException e) {
            throw new AmbiguousElementException();
        }
    }

    /**
     * Return a single value assigned to a key that bounds the given type
     * @throws NoSuchElementException if no such value exists
     * @throws AmbiguousElementException if multiple such values exist
     */
    public V oneAssignableFrom(TypeToken<? extends K> type) {
        try {
            return Iterables.getOnlyElement(allAssignableFrom(type));
        } catch(IllegalArgumentException e) {
            throw new AmbiguousElementException();
        }
    }

    public V oneAssignableTo(Class<? extends K> bounds) {
        return oneAssignableTo(TypeToken.of(bounds));
    }

    public V oneAssignableFrom(Class<? extends K> bounds) {
        return oneAssignableFrom(TypeToken.of(bounds));
    }
}
