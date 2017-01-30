package tc.oc.commons.core.util;

import java.util.Set;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import com.google.common.reflect.TypeToken;

/**
 * A {@link TypeMap} that caches the results of {@link #allAssignableFrom} and {@link #allAssignableTo}.
 * The cache is NEVER invalidated automatically. It must be manually invalidated by calling {@link #invalidate()}
 * whenever the contents are changed.
 */
public class CachingTypeMap<K, V> extends TypeMap<K, V> {

    private final LoadingCache<TypeToken, Set<V>> assignableFrom = CacheUtils.newCache(type -> ImmutableSet.copyOf(super.allAssignableFrom(type)));
    private final LoadingCache<TypeToken, Set<V>> assignableTo = CacheUtils.newCache(type -> ImmutableSet.copyOf(super.allAssignableTo(type)));

    protected CachingTypeMap(SetMultimap<TypeToken<? extends K>, V> map) {
        super(map);
    }

    public static <K, V> CachingTypeMap<K, V> create() {
        return new CachingTypeMap<>(HashMultimap.create());
    }

    public void invalidate() {
        assignableFrom.invalidateAll();
        assignableTo.invalidateAll();
    }

    @Override
    public Set<V> allAssignableTo(TypeToken<? extends K> bounds) {
        return assignableTo.getUnchecked(bounds);
    }

    @Override
    public Set<V> allAssignableFrom(TypeToken<? extends K> type) {
        return assignableFrom.getUnchecked(type);
    }
}
