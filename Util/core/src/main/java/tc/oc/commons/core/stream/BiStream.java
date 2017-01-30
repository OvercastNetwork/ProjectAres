package tc.oc.commons.core.stream;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.Maps;

/**
 * A {@link Stream} of {@link Map.Entry}s with extra methods to operate
 * on the keys or values individually. This tends to be more readable
 * than calling getKey() and getValue() all over the place.
 */
public interface BiStream<K, V> extends Stream<Map.Entry<K, V>> {

    static <K, V> BiStream<K, V> from(Stream<Map.Entry<K, V>> entries) {
        return new BiStreamImpl<>(entries);
    }

    static <K, V> BiStream<K, V> from(Map<K, V> map) {
        return from(map.entrySet().stream());
    }

    static <K, V> BiStream<K, V> fromKeys(Stream<K> keys, Function<? super K, V> mapper) {
        return from(keys.map(key -> Maps.immutableEntry(key, mapper.apply(key))));
    }

    static <K, V> BiStream<K, V> fromValues(Stream<V> values, Function<? super V, K> mapper) {
        return from(values.map(value -> Maps.immutableEntry(mapper.apply(value), value)));
    }

    static <K, V> BiStream<K, V> empty() {
        return from(Stream.empty());
    }

    static <K, V> BiStream<K, V> of(K key, V value) {
        return from(Stream.of(Maps.immutableEntry(key, value)));
    }

    default <T> Stream<T> merge(BiFunction<? super K, ? super V, ? extends T> mapper) {
        return map(entry -> mapper.apply(entry.getKey(), entry.getValue()));
    }

    default <T> Stream<T> flatMerge(BiFunction<? super K, ? super V, ? extends Stream<T>> mapper) {
        return flatMap(entry -> mapper.apply(entry.getKey(), entry.getValue()));
    }

    default Stream<K> keys() {
        return map(Map.Entry::getKey);
    }

    default Stream<V> values() {
        return map(Map.Entry::getValue);
    }

    default BiStream<K, V> filter(BiPredicate<K, V> condition) {
        return from(filter(entry -> condition.test(entry.getKey(), entry.getValue())));
    }

    default BiStream<K, V> filterKeys(Predicate<K> condition) {
        return from(filter(entry -> condition.test(entry.getKey())));
    }

    default BiStream<K, V> filterValues(Predicate<V> condition) {
        return from(filter(entry -> condition.test(entry.getValue())));
    }

    default <RK, RV> BiStream<RK, RV> map(BiFunction<? super K, ? super V, ? extends Map.Entry<RK, RV>> mapper) {
        return from(map(entry -> mapper.apply(entry.getKey(), entry.getValue())));
    }

    default <RK, RV> BiStream<RK, RV> map(Function<? super K, ? extends RK> keyMapper, Function<? super V, ? extends RV> valueMapper) {
        return from(map(entry -> Maps.immutableEntry(keyMapper.apply(entry.getKey()),
                                                     valueMapper.apply(entry.getValue()))));
    }

    default <RK, RV> BiStream<RK, RV> map(BiFunction<? super K, ? super V, ? extends RK> keyMapper, BiFunction<? super K, ? super V, ? extends RV> valueMapper) {
        return from(map(entry -> Maps.immutableEntry(keyMapper.apply(entry.getKey(), entry.getValue()),
                                                     valueMapper.apply(entry.getKey(), entry.getValue()))));
    }

    default <R> BiStream<R, V> mapKeys(Function<? super K, ? extends R> mapper) {
        return from(map(entry -> Maps.immutableEntry(mapper.apply(entry.getKey()),
                                                     entry.getValue())));
    }

    default <R> BiStream<K, R> mapValues(Function<? super V, ? extends R> mapper) {
        return from(map(entry -> Maps.immutableEntry(entry.getKey(),
                                                     mapper.apply(entry.getValue()))));
    }

    default <RK, RV> BiStream<RK, RV> flatMap(BiFunction<? super K, ? super V, ? extends Stream<Map.Entry<RK, RV>>> mapper) {
        return from(flatMap(entry -> mapper.apply(entry.getKey(), entry.getValue())));
    }

    default Optional<Map.Entry<K, V>> maxByKey(Comparator<? super K> comparator) {
        return max((a, b) -> comparator.compare(a.getKey(), b.getKey()));
    }

    default Optional<Map.Entry<K, V>> maxByValue(Comparator<? super V> comparator) {
        return max((a, b) -> comparator.compare(a.getValue(), b.getValue()));
    }

    default Optional<Map.Entry<K, V>> minByKey(Comparator<? super K> comparator) {
        return min((a, b) -> comparator.compare(a.getKey(), b.getKey()));
    }

    default Optional<Map.Entry<K, V>> minByValue(Comparator<? super V> comparator) {
        return min((a, b) -> comparator.compare(a.getValue(), b.getValue()));
    }
}
