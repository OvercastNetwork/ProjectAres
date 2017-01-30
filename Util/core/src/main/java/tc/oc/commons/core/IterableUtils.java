package tc.oc.commons.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import tc.oc.commons.core.reflect.Methods;
import tc.oc.commons.core.util.Comparators;
import tc.oc.commons.core.util.IteratorUtils;
import tc.oc.commons.core.util.Streams;
import tc.oc.commons.core.util.ThrowingConsumer;

/** {@link Iterable}-related utilities. */
public class IterableUtils {

    private static final Method ITERATOR_METHOD = Methods.method(Iterable.class, "iterator");

    public static <T> TypeToken<? extends Iterator<T>> iteratorType(TypeToken<? extends Iterable<T>> iterableType) {
        return (TypeToken<? extends Iterator<T>>) iterableType.method(ITERATOR_METHOD).getReturnType();
    }

    public static <T> TypeToken<T> elementType(TypeToken<? extends Iterable<T>> iterableType) {
        return IteratorUtils.elementType(iteratorType(iterableType));
    }

    /**
     * Finds the most common element in the specified {@link Iterable}. If there is a tie and <code>tie</code> is true,
     * a random selection (from the tied elements) is made. If there is a tie and <code>tie</code> is not true,
     * <code>null</code> is returned, instead.
     *
     * @param iterable The {@link Iterable} to check.
     * @param <T>      The type of the {@link Iterable}.
     * @return The most common element, an arbitrary selection from the tied elements if a tie arises and
     *         <code>tie</code> is true, or <code>null</code> if a tie arises and <code>tie</code> is not true..
     */
    public static @Nullable <T> T findMostCommon(Iterable<T> iterable, boolean tie) {
        HashMap<T, Integer> counts = new HashMap<>();
        for (T obj : Preconditions.checkNotNull(iterable, "Iterable")) {
            Integer count = counts.get(obj);
            counts.put(obj, count == null ? 1 : count + 1);
        }

        int max = 0;
        T maxObj = null;
        for (Map.Entry<T, Integer> entry : counts.entrySet()) {
            int value = entry.getValue();
            if (maxObj == null || value >= max) {
                max = value;
                maxObj = entry.getKey();
            }
        }

        if (!tie && maxObj != null) {
            for (Map.Entry<T, Integer> entry : counts.entrySet()) {
                if (entry.getValue() == max && !maxObj.equals(entry.getKey())) {
                    return null;
                }
            }
        }

        return maxObj;
    }

    /**
     * Finds the most common element in the specified {@link Iterable}. If there is a tie, a random selection (from the
     * tied elements) is made. If this functionality is undesired, {@link #findMostCommon(Iterable, boolean)} may be
     * used instead.
     *
     * @param iterable The {@link Iterable} to check.
     * @param <T>      The type of the {@link Iterable}.
     * @return The most common element, or an arbitrary selection from the tied elements if a tie arises.
     */
    public static <T> T findMostCommon(Iterable<T> iterable) {
        return findMostCommon(iterable, true);
    }

    /**
     * Transform and filter at the same time.
     * Return null from the transform function to skip the current element.
     */
    public static <In, Out> Iterator<Out> transfilter(final Iterator<In> iterator,
                                                      final Function<? super In, ? extends Out> function) {
        return new Iterator<Out>() {
            Out next;

            @Override
            public boolean hasNext() {
                if(next != null) {
                    return true;
                } else {
                    while(iterator.hasNext()) {
                        next = function.apply(iterator.next());
                        if(next != null) return true;
                    }
                    return false;
                }
            }

            @Override
            public Out next() {
                if(!hasNext()) throw new NoSuchElementException();
                Out tmp = next;
                next = null;
                return tmp;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Transform and filter at the same time.
     * Return null from the transform function to skip the current element.
     */
    public static <In, Out> Iterable<Out> transfilter(final Iterable<In> iterable,
                                                      final Function<? super In, ? extends Out> function) {
        return new Iterable<Out>() {
            @Override
            public Iterator<Out> iterator() {
                return transfilter(iterable.iterator(), function) ;
            }
        };
    }

    public static <In, Out> Collection<Out> transfilter(final Collection<In> collection,
                                                        final Function<? super In, ? extends Out> function) {
        return ImmutableList.copyOf(transfilter((Iterable) collection, function));
    }

    public static Iterable<String> toStrings(Iterable<?> things) {
        return Iterables.transform(things, String::valueOf);
    }

    public static Collection<String> toStrings(Collection<?> things) {
        return Collections2.transform(things, String::valueOf);
    }

    /**
     * Return a copy of the given collection in whatever subclass of {@link ImmutableCollection} fits best
     */
    public static <E> ImmutableCollection<E> immutableCopyOf(Collection<E> things) {
        if(things instanceof List) {
            return ImmutableList.copyOf(things);
        } else {
            return ImmutableSet.copyOf(things);
        }
    }

    public static <T> Iterable<T> prepend(T element, Iterable<? extends T> iterable) {
        return Iterables.concat(ImmutableSet.of(element), iterable);
    }

    public static <T> Iterable<T> append(Iterable<? extends T> iterable, T...rest) {
        return Iterables.concat(iterable, Arrays.asList(rest));
    }

    public static <T> void reverseForEach(Iterable<T> iterable, Consumer<? super T> consumer) {
        Streams.reverseStream(iterable).forEach(consumer);
    }

    public static <T, E extends Throwable> void reverseForEachThrows(Iterable<T> iterable, ThrowingConsumer<? super T, E> consumer) throws E {
        Streams.forEachThrows(Streams.reverseStream(iterable), consumer);
    }

    public static <T> Set<T> instancesOf(Set<? super T> set, Class<T> type) {
        return (Set<T>) Sets.filter(set, type::isInstance);
    }

    public static <T> boolean any(Iterable<T> iterable, Predicate<? super T> predicate) {
        for(T t : iterable) {
            if(predicate.test(t)) return true;
        }
        return false;
    }

    public static <T> boolean all(Iterable<T> iterable, Predicate<? super T> predicate) {
        for(T t : iterable) {
            if(!predicate.test(t)) return false;
        }
        return true;
    }

    public static <T> boolean none(Iterable<T> iterable, Predicate<? super T> predicate) {
        for(T t : iterable) {
            if(predicate.test(t)) return false;
        }
        return true;
    }

    public static <T> Iterable<T> emptyIterable() { return EMPTY_ITERABLE; }
    private static final Iterable EMPTY_ITERABLE = Collections::emptyIterator;

    /**
     * Return the first element in the given (non-empty) {@link Iterable}
     * @throws NoSuchElementException if the iterable is empty
     */
    public static <T> T getFirst(Iterable<? extends T> iterable) {
        if(iterable instanceof List) {
            final List<T> list = (List<T>) iterable;
            if(list.isEmpty()) throw new NoSuchElementException();
            return list.get(0);
        } else {
            return iterable.iterator().next();
        }
    }

    /**
     * Return the concatenation of the {@link Iterables} in the given {@link Collection}.
     *
     * Result is equivalent to {@link Iterables#concat}, but there are some optimizations
     * for the cases where size == 0 and size == 1.
     */
    public static <T> Iterable<T> concat(Collection<? extends Iterable<? extends T>> iterables) {
        switch(iterables.size()) {
            case 0: return emptyIterable();
            case 1: return (Iterable<T>) getFirst(iterables);
            default: return Iterables.concat(iterables);
        }
    }

    @SafeVarargs
    public static <T> Iterable<T> concat(Iterable<? extends T>... iterables) {
        switch(iterables.length) {
            case 0: return emptyIterable();
            case 1: return (Iterable<T>) iterables[0];
            default: return Iterables.concat(iterables);
        }
    }

    @SafeVarargs
    public static <T> Iterable<T> unique(Iterable<? extends T>... iterables) {
        return ImmutableSet.copyOf(concat(iterables));
    }

    public static <T> List<T> asList(Iterable<? extends T> iterable) {
        if(iterable instanceof List) {
            return (List<T>) iterable;
        } else {
            return ImmutableList.copyOf(iterable);
        }
    }

    public static <T> T randomElement(Iterable<? extends T> iterable, Random random) {
        return ListUtils.randomElement(asList(iterable), random);
    }

    public static <T> ImmutableCollection<T> copyOf(Iterable<T> iterable) {
        if(iterable instanceof Set) {
            return ImmutableSet.copyOf(iterable);
        } else if(iterable instanceof Multiset) {
            return ImmutableMultiset.copyOf(iterable);
        } else {
            return ImmutableList.copyOf(iterable);
        }
    }

    public static <T> T removeNext(Iterator<T> iterator) {
        final T t = iterator.next();
        iterator.remove();
        return t;
    }

    public static <T> T removeFirst(Iterable<T> iterable) {
        return removeNext(iterable.iterator());
    }

    public static <T> T unify(Iterable<? extends T> things, T empty, Function<Iterable<? extends T>, ? extends T> unifier) {
        if(things instanceof Collection) {
            final Collection<? extends T> collection = (Collection<? extends T>) things;
            switch(collection.size()) {
                case 0: return empty;
                case 1: return collection.iterator().next();
                default: return unifier.apply(things);
            }
        } else {
            final Iterator<? extends T> iterator = things.iterator();
            if(!iterator.hasNext()) return empty;

            final T thing = iterator.next();
            if(!iterator.hasNext()) return thing;

            return unifier.apply(things);
        }
    }

    public static <T> Iterable<T> sorted(Iterable<T> unsorted, Comparator<? super T> order) {
        final List<T> sorted = Lists.newArrayList(unsorted);
        sorted.sort(order);
        return sorted;
    }

    public static <T extends Comparable<? super T>> Iterable<T> ascending(Iterable<T> unsorted) {
        return sorted(unsorted, Comparator.naturalOrder());
    }

    public static <T extends Comparable<? super T>> Iterable<T> descending(Iterable<T> unsorted) {
        return sorted(unsorted, Comparator.reverseOrder());
    }
}
