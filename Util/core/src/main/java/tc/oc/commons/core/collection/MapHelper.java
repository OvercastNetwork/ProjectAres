package tc.oc.commons.core.collection;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.common.collect.ForwardingMap;
import tc.oc.commons.core.stream.BiStream;

public interface MapHelper<K, V> extends Map<K, V> {

    static <K, V> MapHelper<K, V> wrap(Map<K, V> map) {
        return new Wrapper<>(map);
    }

    default void removeIf(BiPredicate<K, V> filter) {
        entrySet().removeIf(entry -> filter.test(entry.getKey(), entry.getValue()));
    }

    default void retainIf(BiPredicate<K, V> filter) {
        entrySet().removeIf(entry -> !filter.test(entry.getKey(), entry.getValue()));
    }

    default void removeIfValue(Predicate<V> filter) {
        values().removeIf(filter);
    }

    default void putAbsent(Map<K, V> src) {
        for(Map.Entry<K, V> entry : src.entrySet()) {
            if(!containsKey(entry.getKey())) {
                put(entry.getKey(), entry.getValue());
            }
        }
    }

    default void putAll(Collection<K> src, V value) {
        for(K k : src) {
            put(k, value);
        }
    }

    default Stream<K> keyStream() {
        return keySet().stream();
    }

    default Stream<V> valueStream() {
        return values().stream();
    }

    default Stream<Map.Entry<K, V>> entryStream() {
        return entrySet().stream();
    }

    default BiStream<K, V> stream() {
        return BiStream.from(this);
    }

    default Optional<V> value(K key) {
        return Optional.ofNullable(get(key));
    }

    default Optional<V> ifPresent(K key, Consumer<V> consumer) {
        final Optional<V> value = value(key);
        value.ifPresent(consumer);
        return value;
    }

    /**
     * Alternative to {@link #computeIfAbsent(Object, Function)} that takes a
     * {@link Supplier} instead of a {@link Function}. Usually, the caller already has
     * the key, since they just passed it to this method, and having to declare a
     * duplicate key variable for the lambda is just annoying.
     */
    default V computeIfAbsent(K key, Supplier<V> computer) {
        return computeIfAbsent(key, key0 -> computer.get());
    }
}

class Wrapper<K, V> extends ForwardingMap<K, V> implements MapHelper<K, V> {
    private final Map<K, V> delegate;

    Wrapper(Map<K, V> delegate) {
        this.delegate = delegate;
    }

    @Override
    protected Map<K, V> delegate() {
        return delegate;
    }
}