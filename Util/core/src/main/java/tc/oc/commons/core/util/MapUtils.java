package tc.oc.commons.core.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

public class MapUtils {
    private MapUtils() {}

    public static <K, V> void putAbsent(Map<K, V> dest, Map<K, V> src) {
        for(Map.Entry<K, V> entry : src.entrySet()) {
            if(!dest.containsKey(entry.getKey())) {
                dest.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public static <K, V> void putAll(Map<K, V> dest, Collection<K> src, V value) {
        for(K k : src) {
            dest.put(k, value);
        }
    }

    public static <K, V> ImmutableMap<K, V> merge(Map<K, V> dest, Map<K, V> src) {
        ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
        builder.putAll(src);
        for(Map.Entry<K, V> entry : dest.entrySet()) {
            if(!src.containsKey(entry.getKey())) builder.put(entry);
        }
        return builder.build();
    }

    public static <K, V> ImmutableMap<K, V> merge(Map<K, V> dest, K key, V value) {
        ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
        builder.put(key, value);
        for(Map.Entry<K, V> entry : dest.entrySet()) {
            if(!key.equals(entry.getKey())) builder.put(entry);
        }
        return builder.build();
    }

    public static <K, V> Multimap<K, V> combine(Iterable<Multimap<K, V>> maps) {
        Multimap<K, V> singleton = null;
        ImmutableMultimap.Builder<K, V> builder = null;
        for(Multimap<K, V> map : maps) {
            if(!map.isEmpty()) {
                if(singleton == null) {
                    singleton = map;
                } else {
                    if(builder == null) {
                        builder = ImmutableMultimap.builder();
                    }
                    builder.putAll(singleton);
                    builder.putAll(map);
                }
            }
        }

        if(builder != null) {
            return builder.build();
        } else if(singleton != null) {
            return singleton;
        } else {
            return ImmutableMultimap.of();
        }
    }

    public static <K, V> Multimap<K, V> combine(Multimap<K, V>... maps) {
        return combine(Arrays.asList(maps));
    }

    public static <K, V, R> Stream<R> mapEntries(Map<K, V> map, BiFunction<K, V, R> mapper) {
        Stream.Builder<R> builder = Stream.builder();
        map.forEach((k, v) -> builder.add(mapper.apply(k, v)));
        return builder.build();
    }

    public static <K, V, R> Stream<R> mapEntries(Multimap<K, V> map, BiFunction<K, V, R> mapper) {
        Stream.Builder<R> builder = Stream.builder();
        map.entries().forEach(e -> builder.add(mapper.apply(e.getKey(), e.getValue())));
        return builder.build();
    }

    public static <K1, V, K2> Map<K2, V> transformKeys(Map<K1, V> map, Function<K1, K2> keyMapper) {
        final ImmutableMap.Builder<K2, V> builder = ImmutableMap.builder();
        map.forEach((k, v) -> builder.put(keyMapper.apply(k), v));
        return builder.build();
    }

    public static <K1, V, K2> Map<K2, V> transformKeys(Map<K1, V> map, BiFunction<K1, V, K2> keyMapper) {
        final ImmutableMap.Builder<K2, V> builder = ImmutableMap.builder();
        map.forEach((k, v) -> builder.put(keyMapper.apply(k, v), v));
        return builder.build();
    }

    public static <K, V1, V2> Map<K, V2> transformValues(Map<K, V1> map, Function<V1, V2> valueMapper) {
        final ImmutableMap.Builder<K, V2> builder = ImmutableMap.builder();
        map.forEach((k, v) -> builder.put(k, valueMapper.apply(v)));
        return builder.build();
    }

    public static <K, V1, V2> Map<K, V2> transformValues(Map<K, V1> map, BiFunction<K, V1, V2> valueMapper) {
        final ImmutableMap.Builder<K, V2> builder = ImmutableMap.builder();
        map.forEach((k, v) -> builder.put(k, valueMapper.apply(k, v)));
        return builder.build();
    }

    public static <K, V> Optional<V> value(Map<K, V> map, K key) {
        return Optional.ofNullable(map.get(key));
    }

    public static <R, C, V> Optional<V> value(Table<R, C, V> table, R rowKey, C columnKey) {
        return Optional.ofNullable(table.get(rowKey, columnKey));
    }

    public static <K, V> Optional<V> ifPresent(Map<K, V> map, K key, Consumer<V> consumer) {
        final Optional<V> value = value(map, key);
        value.ifPresent(consumer);
        return value;
    }

    /**
     * Alternative to {@link #computeIfAbsent(Map, Object, Supplier)} that takes a
     * {@link Supplier} instead of a {@link Function}. Usually, the caller already has
     * the key, since they just passed it to this method, and having to declare a
     * duplicate key variable for the lambda is just annoying.
     */
    public static <K, V> V computeIfAbsent(Map<K, V> map, K key, Supplier<V> computer) {
        return computeIfAbsent(map, key, key0 -> computer.get());
    }

    /**
     * A version of {@link Map#computeIfAbsent(Object, Function)} that allows you to
     * safely access the map from inside the compute function. Some of the specialized
     * implementations of the original method in the JDK (e.g. the one in
     * {@link java.util.HashMap}) can put the map in an illegal state if you try to
     * do that.
     */
    public static <K, V> V computeIfAbsent(Map<K, V> map, K key, Function<K, V> computer) {
        V value = map.get(key);
        if(value == null) {
            value = computer.apply(key);
            if(value != null) {
                map.put(key, value);
            }
        }
        return value;
    }

    public static <K, V> void forEach(Multimap<K, V> multimap, BiConsumer<? super K, ? super V> consumer) {
        multimap.asMap().forEach((key, values) -> values.forEach(value -> consumer.accept(key, value)));
    }

    public static <K, V> void forEachWithIndex(Map<K, V> map, int start, IndexedBiConsumer<? super K, ? super V> consumer) {
        final Counter index = new Counter(start);
        map.forEach((k, v) -> consumer.accept(k, v, index.next()));
    }

    public static <K, V> void forEachWithIndex(Map<K, V> map, IndexedBiConsumer<? super K, ? super V> consumer) {
        forEachWithIndex(map, 0, consumer);
    }

    public static <K, V> void forEachWithIndex(Multimap<K, V> multimap, int start, IndexedBiConsumer<? super K, ? super V> consumer) {
        final Counter index = new Counter(start);
        forEach(multimap, (k, v) -> consumer.accept(k, v, index.next()));
    }

    public static <K, V> void forEachWithIndex(Multimap<K, V> multimap, IndexedBiConsumer<? super K, ? super V> consumer) {
        forEachWithIndex(multimap, 0, consumer);
    }
}
