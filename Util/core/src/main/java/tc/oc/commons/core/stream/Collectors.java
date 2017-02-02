package tc.oc.commons.core.stream;

import java.util.ArrayList;
import java.util.Collections;
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
import java.util.stream.Stream;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
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

    public static <T, K> Collector<T, ?, ImmutableSetMultimap<K, T>> toImmutableSetMultimap(Function<? super T, ? extends K> keyMapper) {
        return toImmutableSetMultimap(keyMapper, identity());
    }

    public static <T, K, V> Collector<T, ?, ImmutableSetMultimap<K, V>> toImmutableSetMultimap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        return new ImmutableMultimapCollector<>(ImmutableSetMultimap::builder, t -> Stream.of(keyMapper.apply(t)), t -> Stream.of(valueMapper.apply(t)));
    }

    private static class ImmutableMultimapCollector<T, K, V, A extends ImmutableMultimap.Builder<K, V>, R extends ImmutableMultimap<K, V>> implements Collector<T, A, R> {

        private final Supplier<A> builderSupplier;
        private final Function<? super T, Stream<? extends K>> keyMapper;
        private final Function<? super T, Stream<? extends V>> valueMapper;

        private ImmutableMultimapCollector(Supplier<A> builderSupplier, Function<? super T, Stream<? extends K>> keyMapper, Function<? super T, Stream<? extends V>> valueMapper) {
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
                keyMapper.apply(t).forEach(
                    key -> valueMapper.apply(t).forEach(
                        value -> builder.put(key, value)
                    )
                );
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

    public static <T, K> Collector<T, ?, SetMultimap<K, T>> indexingByMulti(Function<? super T, Stream<? extends K>> keyMapper) {
        return new MultimapCollector<>(HashMultimap::create, keyMapper, Stream::of);
    }

    public static <T, V> Collector<T, ?, SetMultimap<T, V>> mappingToMulti(Function<? super T, Stream<? extends V>> valueMapper) {
        return new MultimapCollector<>(HashMultimap::create, Stream::of, valueMapper);
    }

    private static class MultimapCollector<T, K, V, A extends Multimap> implements Collector<T, A, A> {

        private final Supplier<A> multimapSupplier;
        private final Function<? super T, Stream<? extends K>> keyMapper;
        private final Function<? super T, Stream<? extends V>> valueMapper;

        private MultimapCollector(Supplier<A> multimapSupplier, Function<? super T, Stream<? extends K>> keyMapper, Function<? super T, Stream<? extends V>> valueMapper) {
            this.multimapSupplier = multimapSupplier;
            this.keyMapper = keyMapper;
            this.valueMapper = valueMapper;
        }

        @Override
        public Supplier<A> supplier() {
            return multimapSupplier;
        }

        @Override
        public BiConsumer<A, T> accumulator() {
            return (multimap, t) -> {
                keyMapper.apply(t).forEach(
                    key -> valueMapper.apply(t).forEach(
                        value -> multimap.put(key, value)
                    )
                );
            };
        }

        @Override
        public BinaryOperator<A> combiner() {
            return (m1, m2) -> {
                m1.putAll(m2);
                return m1;
            };
        }

        @Override
        public Function<A, A> finisher() {
            return Function.identity();
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Collections.singleton(Characteristics.IDENTITY_FINISH);
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
