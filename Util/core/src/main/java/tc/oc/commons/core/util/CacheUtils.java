package tc.oc.commons.core.util;

import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public interface CacheUtils {
    static <K, V> CacheBuilder<K, V> newBuilder() {
        return (CacheBuilder<K, V>) CacheBuilder.newBuilder();
    }

    static <K, V, E extends Exception> LoadingCache<K, V> newCache(CacheBuilder<K, V> builder, ThrowingFunction<K, V, E> loader) {
        return builder.build(new CacheLoader<K, V>() {
            @Override
            public V load(K key) throws E {
                return loader.applyThrows(key);
            }
        });
    }

    static <K, V, E extends Exception> LoadingCache<K, V> newCache(ThrowingFunction<K, V, E> loader) {
        return newCache(newBuilder(), loader);
    }

    static <K, V> Cache<K, V> newCache() {
        return newBuilder().build();
    }

    static <K, V, E extends Exception> LoadingCache<K, V> newWeakKeyCache(ThrowingFunction<K, V, E> loader) {
        return newCache((CacheBuilder<K, V>) newBuilder().weakKeys(), loader);
    }

    static <K, V> V getUnchecked(Cache<K, V> cache, K key, Supplier<V> supplier) {
        try {
            return cache.get(key, supplier::get);
        } catch(ExecutionException e) {
            throw Throwables.propagate(e);
        }
    }
}
