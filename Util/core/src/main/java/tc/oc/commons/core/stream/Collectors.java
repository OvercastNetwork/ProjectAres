package tc.oc.commons.core.stream;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Lists;
import tc.oc.commons.core.random.Entropy;
import tc.oc.commons.core.random.MutableEntropy;
import tc.oc.commons.core.util.AmbiguousElementException;

import static java.util.Collections.emptySet;
import static java.util.function.Function.identity;

public final class Collectors {
    private Collectors() {}

    public static <T> Collector<T, ?, Optional<T>> zeroOrOne() {
        return java.util.stream.Collectors.reducing((a, b) -> { throw new AmbiguousElementException(); });
    }

    public static <T> Collector<T, ?, ImmutableList<T>> toImmutableList() {
        return new ListCollector<>(ImmutableList::copyOf);
    }

    public static <T> Collector<T, ?, ImmutableList<T>> toReverseImmutableList() {
        return new ListCollector<>(f -> ImmutableList.copyOf(Lists.reverse(f)));
    }

    public static <T> Collector<T, ?, ArrayList<T>> toArrayList() {
        return new ListCollector<>(identity());
    }

    public static <T> Collector<T, ?, ImmutableSet<T>> toImmutableSet() {
        return new ListCollector<>(ImmutableSet::copyOf);
    }

    public static <K, V> Collector<Map.Entry<K, V>, ?, ImmutableMap<K, V>> toImmutableMap() {
        return toImmutableMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    public static <T, K> Collector<T, ?, ImmutableMap<K, T>> indexingBy(Function<? super T, ? extends K> keyMapper) {
        return toImmutableMap(keyMapper, identity());
    }

    public static <T, V> Collector<T, ?, ImmutableMap<T, V>> mappingTo(Function<? super T, ? extends V> valueMapper) {
        return toImmutableMap(identity(), valueMapper);
    }

    public static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableMap(Function<? super T, ? extends K> keyMapper,
                                                                               Function<? super T, ? extends V> valueMapper) {
        return new Collector<T, ImmutableMap.Builder<K, V>, ImmutableMap<K, V>>() {
            @Override
            public Supplier<ImmutableMap.Builder<K, V>> supplier() {
                return ImmutableMap::builder;
            }

            @Override
            public BiConsumer<ImmutableMap.Builder<K, V>, T> accumulator() {
                return (builder, t) -> {
                    final V value = valueMapper.apply(t);
                    if(value != null) {
                        builder.put(keyMapper.apply(t), value);
                    }
                };
            }

            @Override
            public BinaryOperator<ImmutableMap.Builder<K, V>> combiner() {
                return (b1, b2) -> {
                    b1.putAll(b2.build());
                    return b1;
                };
            }

            @Override
            public Function<ImmutableMap.Builder<K, V>, ImmutableMap<K, V>> finisher() {
                return ImmutableMap.Builder::build;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return emptySet();
            }
        };
    }

    public static <T, K, V> Collector<T, ?, ImmutableListMultimap<K, V>> toListMultimap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        return new MultimapCollector<>(ImmutableListMultimap::builder, keyMapper, valueMapper);
    }

    public static <T, K> Collector<T, ?, ImmutableSetMultimap<K, T>> toSetMultimap(Function<? super T, ? extends K> keyMapper) {
        return toSetMultimap(keyMapper, identity());
    }

    public static <T, K, V> Collector<T, ?, ImmutableSetMultimap<K, V>> toSetMultimap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        return new MultimapCollector<>(ImmutableSetMultimap::builder, keyMapper, valueMapper);
    }

    private static class MultimapCollector<T, K, V, A extends ImmutableMultimap.Builder<K, V>, R extends ImmutableMultimap<K, V>> implements Collector<T, A, R> {

        private final Supplier<A> builderSupplier;
        private final Function<? super T, ? extends K> keyMapper;
        private final Function<? super T, ? extends V> valueMapper;

        private MultimapCollector(Supplier<A> builderSupplier, Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
            this.builderSupplier = builderSupplier;
            this.keyMapper = keyMapper;
            this.valueMapper = valueMapper;
        }

        @Override
        public Supplier<A> supplier() {
            return builderSupplier;
        }

        @Override
        public BiConsumer<A, T> accumulator() {
            return (builder, t) -> {
                final K key = keyMapper.apply(t);
                final V value = valueMapper.apply(t);
                if(key != null && value != null) {
                    builder.put(key, value);
                }
            };
        }

        @Override
        public BinaryOperator<A> combiner() {
            return (b1, b2) -> {
                // No ideal way to do this with the Builder API
                // https://github.com/google/guava/issues/1582
                b1.putAll(b2.build());
                return b1;
            };
        }

        @Override
        public Function<A, R> finisher() {
            return builder -> (R) builder.build();
        }

        @Override
        public Set<Characteristics> characteristics() {
            return emptySet();
        }
    }

    private static class ListCollector<T, R> implements Collector<T, ArrayList<T>, R> {

        private final Function<ArrayList<T>, R> finisher;

        protected ListCollector(Function<ArrayList<T>, R> finisher) {
            this.finisher = finisher;
        }

        @Override
        public Supplier<ArrayList<T>> supplier() {
            return ArrayList::new;
        }

        @Override
        public BiConsumer<ArrayList<T>, T> accumulator() {
            return List::add;
        }

        @Override
        public BinaryOperator<ArrayList<T>> combiner() {
            return (list1, list2) -> {
                list1.addAll(list2);
                return list1;
            };
        }

        @Override
        public Function<ArrayList<T>, R> finisher() {
            return finisher;
        }

        @Override
        public Set<Collector.Characteristics> characteristics() {
            return emptySet();
        }
    }

    public static <T> Collector<T, ?, ArrayList<T>> toRandomSubList(Entropy entropy, int size) {
        return new RandomSubListCollector<>(entropy, size);
    }

    public static <T> Collector<T, ?, ArrayList<T>> toRandomSubList(int size) {
        return new RandomSubListCollector<>(new MutableEntropy(), size);
    }

    public static <T> Collector<T, ?, Optional<T>> toRandomElement(Entropy entropy) {
        return new RandomElementCollector<>(entropy);
    }

    public static <T> Collector<T, ?, Optional<T>> toRandomElement() {
        return new RandomElementCollector<>(new MutableEntropy());
    }

    private static class RandomElementCollector<T> extends RandomCollector<T, Optional<T>> {

        public RandomElementCollector(Entropy entropy) {
            super(entropy, 1);
        }

        @Override
        public Function<ArrayList<T>, Optional<T>> finisher() {
            return list -> Optional.ofNullable(list.isEmpty() ? null : list.get(0));
        }

    }

    private static class RandomSubListCollector<T> extends RandomCollector<T, ArrayList<T>> {

        public RandomSubListCollector(Entropy entropy, int size) {
            super(entropy, size);
        }

        @Override
        public Function<ArrayList<T>, ArrayList<T>> finisher() {
            return Function.identity();
        }

    }

    private static abstract class RandomCollector<T, R> implements Collector<T, ArrayList<T>, R> {

        private final Entropy entropy;
        private final int size;
        private int seen = 0;

        public RandomCollector(Entropy entropy, int size) {
            this.entropy = entropy;
            this.size = size;
        }

        @Override
        public Supplier<ArrayList<T>> supplier() {
            return ArrayList::new;
        }

        @Override
        public BiConsumer<ArrayList<T>, T> accumulator() {
            return (list, element) -> {
                if(list.size() < size) {
                    list.add(element);
                } else {
                    int replaceIndex = (int) (entropy.randomDouble() * (size + 1 + seen++));
                    if(replaceIndex < size) {
                        list.set(replaceIndex, element);
                    }
                }
            };
        }

        @Override
        public BinaryOperator<ArrayList<T>> combiner() {
            return (left, right) -> {
                left.addAll(right);
                return left;
            };
        }

        @Override
        public Set<java.util.stream.Collector.Characteristics> characteristics() {
            return EnumSet.of(Collector.Characteristics.UNORDERED, Collector.Characteristics.IDENTITY_FINISH);
        }

    }

}
