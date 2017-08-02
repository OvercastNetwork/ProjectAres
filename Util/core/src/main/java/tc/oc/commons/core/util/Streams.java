package tc.oc.commons.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import tc.oc.commons.core.IterableUtils;

public final class Streams {
    private Streams() {}

    private static final Consumer NO_OP = o -> {};
    public static void consume(Stream<?> stream) {
        stream.forEach(NO_OP);
    }

    public static <T> Stream<T> reverseStream(Iterable<T> iterable) {
        return Lists.reverse(Lists.newArrayList(iterable)).stream();
    }

    public static <T, E extends Throwable> void forEachThrows(Stream<T> stream, ThrowingConsumer<? super T, E> consumer) throws E {
        for(final Iterator<T> it = stream.iterator(); it.hasNext();) {
            consumer.acceptThrows(it.next());
        }
    }

    public static <T> void forEachWithIndex(Stream<T> stream, IndexedConsumer<T> consumer) {
        final Counter index = new Counter();
        stream.forEachOrdered(t -> consumer.accept(t, index.next()));
    }

    public static <T> Stream<T> reverse(Stream<T> stream) {
        return Lists.reverse(stream.collect(Collectors.toCollection(ArrayList::new))).stream();
    }

    public static <T> void reverseForEach(Stream<T> stream, Consumer<? super T> consumer) {
        reverse(stream).forEach(consumer);
    }

    public static <T, E extends Throwable> void reverseForEachThrows(Stream<T> stream, ThrowingConsumer<? super T, E> consumer) throws E {
        forEachThrows(reverse(stream), consumer);
    }

    public static <T> Stream<T> shuffle(Stream<T> stream, Random random) {
        final List<T> list = stream.collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(list, random);
        return list.stream();
    }

    public static <T> Stream<T> shuffle(Stream<T> stream) {
        final List<T> list = stream.collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(list);
        return list.stream();
    }

    public static <T> Stream<T> flatten(Stream<Stream<? extends T>> streams) {
        return (Stream<T>) streams.reduce(Stream::concat)
                                  .orElse(Stream.of());
    }

    public static <T> Stream<T> concat(Stream<? extends T>... streams) {
        return flatten(Stream.of(streams));
    }

    public static <T> Stream<T> instancesOf(Stream<?> stream, Class<T> type) {
        return (Stream<T>) stream.filter(type::isInstance);
    }

    public static <T> Stream<Class<? extends T>> subtypesOf(Stream<? extends Class<?>> stream, Class<T> type) {
        return (Stream) stream.filter(type::isAssignableFrom);
    }

    public static <T> boolean any(Stream<T> stream, Predicate<? super T> predicate) {
        return stream.filter(predicate).findAny().isPresent();
    }

    public static <T> boolean all(Stream<T> stream, Predicate<? super T> predicate) {
        return any(stream, predicate.negate());
    }

    public static <T> boolean none(Stream<T> stream, Predicate<? super T> predicate) {
        return !any(stream, predicate);
    }

    public static <T> Stream<T> copyOf(Stream<T> stream) {
        return stream.collect(Collectors.toList()).stream();
    }

    public static <T> Stream<T> copyOf(Collection<T> collection) {
        return IterableUtils.copyOf(collection).stream();
    }

    public static <T> Stream<T> append(Stream<T> stream, T... elements) {
        return Stream.concat(stream, Stream.of(elements));
    }

    public static <T> Stream<T> prepend(Stream<T> stream, T... elements) {
        return Stream.concat(Stream.of(elements), stream);
    }

    public static <T> Stream<T> remove(Stream<T> stream, T... elements) {
        return stream.filter(t -> !ArrayUtils.contains(elements, t));
    }

    public static <T> Stream<T> of(Iterator<T> iterator) {
        return StreamSupport.stream(Spliterators.spliterator(iterator, 0, Spliterator.ORDERED), false);
    }

    public static <T> Stream<T> of(Iterable<T> iterable) {
        if(iterable instanceof Collection) {
            return ((Collection<T>) iterable).stream();
        } else {
            return StreamSupport.stream(iterable.spliterator(), false);
        }
    }

    public static <T> Stream<T> ofNullable(@Nullable T t) {
        return t == null ? Stream.empty() : Stream.of(t);
    }

    public static <T> Stream<T> ofNullables(Stream<T> s) {
        return s.filter(t -> t != null);
    }

    public static <T> Stream<T> ofNullables(T... things) {
        return ofNullables(Stream.of(things));
    }

    public static <T> Stream<T> conditional(boolean condition, Stream<T> stream) {
        return condition ? stream : Stream.empty();
    }

    public static <T> Stream<T> conditional(boolean condition, T element) {
        return condition ? Stream.of(element) : Stream.empty();
    }

    public static <T> Stream<T> conditional(boolean condition, Supplier<? extends T> element) {
        return condition ? Stream.of(element.get()) : Stream.empty();
    }

    public static <T> Stream<T> compact(Stream<Optional<T>> stream) {
        return stream.filter(Optional::isPresent).map(Optional::get);
    }

    public static <T> Stream<T> compact(Optional<T>... elements) {
        return compact(Stream.of(elements));
    }

    public static <T> Stream<T> compact1(T t1, Optional<T>... elements) {
        return Stream.concat(Stream.of(t1), compact(Stream.of(elements)));
    }

    public static <T> Stream<T> compact2(T t1, T t2, Optional<T>... elements) {
        return Stream.concat(Stream.of(t1, t2), compact(Stream.of(elements)));
    }

    public static <T> Stream<T> compact3(T t1, T t2, T t3, Optional<T>... elements) {
        return Stream.concat(Stream.of(t1, t2, t3), compact(Stream.of(elements)));
    }

    public static <T, R> R reduce(Stream<T> stream, R identity, BiFunction<R, T, R> accumulator) {
        class Result { R v; }
        Result result = new Result();
        result.v = identity;
        stream.forEachOrdered(t -> result.v = accumulator.apply(result.v, t));
        return result.v;
    }

    /**
     * Test if all elements of the stream are equal to each other, according to {@link Objects#equals(Object, Object)}.
     * Returns true for an empty stream, or a stream of all nulls.
     */
    public static <T> boolean isUniform(Stream<T> stream) {
        return stream.reduce(Uniformity.EMPTY, Uniformity::add, Uniformity::combine).isUniform();
    }

    /**
     * This is surprisingly complex, but it is the simplest approach I could come up
     * with that is actually in the spirit of streams. This is actually quite efficient,
     * creating at most one temporary object per reduction thread.
     */
    private interface Uniformity<T> {
        boolean isUniform();
        Uniformity<T> add(T t);
        Uniformity<T> combine(Uniformity<T> that);

        // Initial (empty) result
        Uniformity EMPTY = new Uniformity() {
            @Override public boolean isUniform() { return true; }
            @Override public Uniformity add(Object t) { return new Intermediate(t); }
            @Override public Uniformity combine(Uniformity that) { return that; }
        };

        // Result after multiple distinct values have been seen
        Uniformity FAILED = new Uniformity() {
            @Override public boolean isUniform() { return false; }
            @Override public Uniformity add(Object t) { return this; }
            @Override public Uniformity combine(Uniformity that) { return this; }
        };

        // Result after one or more values have been seen, and they are all equal
        class Intermediate<T> implements Uniformity<T> {
            final T value;

            public Intermediate(T value) {
                this.value = value;
            }

            @Override public boolean isUniform() {
                return true;
            }

            @Override public Uniformity<T> add(T t) {
                return Objects.equals(value, t) ? this : FAILED;
            }

            @Override public Uniformity<T> combine(Uniformity<T> that) {
                if(that instanceof Intermediate) {
                    return add(((Intermediate<T>) that).value);
                } else {
                    return that.combine(this);
                }
            }
        }
    }
}
