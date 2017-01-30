package tc.oc.commons.core.util;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.google.common.reflect.TypeToken;


public class ImmutableTypeMap<K, V> extends CachingTypeMap<K, V> {

    private ImmutableTypeMap(SetMultimap<TypeToken<? extends K>, V> map) {
        super(map);
    }

    public static <K, V> ImmutableTypeMap<K, V> of() {
        return new ImmutableTypeMap<>(ImmutableSetMultimap.of());
    }

    public static <K, V> ImmutableTypeMap<K, V> copyOf(Map<TypeToken<? extends K>, Set<V>> map) {
        final Builder<K, V> builder = builder();
        for(TypeToken<? extends K> key : map.keySet()) {
            builder.putAll(key, map.get(key));
        }
        return builder.build();
    }

    public static <K, V> ImmutableTypeMap<K, V> copyOf(SetMultimap<TypeToken<? extends K>, ? extends V> map) {
        return new ImmutableTypeMap<>(ImmutableSetMultimap.copyOf(map));
    }

    public static <K, V> ImmutableTypeMap<K, V> copyOf(TypeMap<? extends K, ? extends V> that) {
        if(that instanceof ImmutableTypeMap) {
            return (ImmutableTypeMap<K, V>) that;
        } else {
            return copyOf((SetMultimap) that);
        }
    }

    public static <K, V> Builder<K, V> builder() {
        return new Builder<>();
    }

    public static class Builder<K, V> {

        private final ImmutableSetMultimap.Builder<TypeToken<? extends K>, V> backing = ImmutableSetMultimap.builder();

        public ImmutableTypeMap<K, V> build() {
            return new ImmutableTypeMap<>(backing.build());
        }

        public ImmutableTypeMap.Builder<K, V> put(TypeToken<? extends K> key, V value) {
            backing.put(key, value);
            return this;
        }

        public ImmutableTypeMap.Builder<K, V> put(Class<? extends K> key, V value) {
            return put(TypeToken.of(key), value);
        }

        public ImmutableTypeMap.Builder<K, V> put(Map.Entry<? extends TypeToken<? extends K>, ? extends V> entry) {
            backing.put(entry);
            return this;
        }

        public ImmutableTypeMap.Builder<K, V> putAll(TypeToken<? extends K> key, Iterable<? extends V> values) {
            backing.putAll(key, values);
            return this;
        }

        public ImmutableTypeMap.Builder<K, V> putAll(Class<? extends K> key, Iterable<? extends V> values) {
            return putAll(TypeToken.of(key), values);
        }

        @SafeVarargs
        public final ImmutableTypeMap.Builder<K, V> putAll(TypeToken<? extends K> key, V... values) {
            backing.putAll(key, values);
            return this;
        }

        @SafeVarargs
        public final ImmutableTypeMap.Builder<K, V> putAll(Class<? extends K> key, V... values) {
            return putAll(TypeToken.of(key), values);
        }

        public ImmutableTypeMap.Builder<K, V> putAll(Multimap<? extends TypeToken<? extends K>, ? extends V> multimap) {
            backing.putAll(multimap);
            return this;
        }
    }
}
