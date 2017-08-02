package tc.oc.commons.core;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import gnu.trove.list.TIntList;
import com.google.common.reflect.TypeToken;
import tc.oc.commons.core.reflect.Methods;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.commons.core.util.Streams;

/** List-related utilities. */
public class ListUtils {
    /**
     * Creates a union from all of the specified {@link List}s.
     *
     * @param lists The {@link List}s.
     * @param <T>   The type of {@link List}s.
     * @return The union.
     */
    @SafeVarargs
    public static <T> List<T> union(List<T>... lists) {
        List<T> newList = new LinkedList<>();

        for (List<T> list : lists) {
            newList.addAll(list);
        }

        return newList;
    }

    public static int indexOf(List<?> list, Object element, int begin, int end) {
        begin = Math.min(begin, list.size());
        end = Math.min(end, list.size());

        if(begin == 0 && end == list.size()) return list.indexOf(element);

        for(int i = begin; i < end; i++) {
            if(Objects.equals(element, list.get(i))) return i;
        }

        return -1;
    }

    public static int indexOf(List<?> list, Object element, int begin) {
        return indexOf(list, element, begin, list.size());
    }

    public static boolean contains(List<?> list, Object element, int begin, int end) {
        return indexOf(list, element, begin, end) != -1;
    }

    public static boolean contains(List<?> list, Object element, int begin) {
        return contains(list, element, begin, list.size());
    }

    public static <T> T randomElement(List<? extends T> list, Random random) {
        if(list.isEmpty()) throw new IndexOutOfBoundsException("List is empty");
        return list.get(random.nextInt(list.size()));
    }

    public static <T> List<T> append(List<T> list, T... elements) {
        if(elements.length == 0) {
            return list;
        } else {
            final ImmutableList.Builder<T> builder = ImmutableList.builder();
            builder.addAll(list);
            builder.add(elements);
            return builder.build();
        }
    }

    public static <E> int lexicalCompare(List<? extends E> a, List<? extends E> b, Comparator<? super E> comp) {
        final int size = Math.min(a.size(), b.size());
        for(int i = 0; i < size; i++) {
            final int c = comp.compare(a.get(i), b.get(i));
            if(c != 0) return c;
        }
        return Integer.compare(a.size(), b.size());
    }

    public static <E extends Comparable<E>> int lexicalCompare(List<? extends E> a, List<? extends E> b) {
        return lexicalCompare(a, b, Comparator.naturalOrder());
    }

    public static int lexicalCompare(TIntList a, TIntList b) {
        final int size = Math.min(a.size(), b.size());
        for(int i = 0; i < size; i++) {
            final int c = Integer.compare(a.get(i), b.get(i));
            if(c != 0) return c;
        }
        return Integer.compare(a.size(), b.size());
    }

    private static final Method GET_METHOD = Methods.method(List.class, "get", int.class);

    public static <T> TypeToken<T> elementType(TypeToken<? extends List<T>> listType) {
        return (TypeToken<T>) listType.method(GET_METHOD).getReturnType();
    }

    public static <F, T> ImmutableList<T> transformedCopyOf(Iterable<F> source, Function<? super F, ? extends T> transform) {
        return Streams.of(source)
                      .map(transform)
                      .collect(Collectors.toImmutableList());
    }

    public static <T> ImmutableList<T> filteredCopyOf(Iterable<T> source, Predicate<? super T> filter) {
        return Streams.of(source)
                      .filter(filter)
                      .collect(Collectors.toImmutableList());
    }

    public static <T> Optional<T> getIfPresent(List<T> list, int index) {
        try {
            return Optional.of(list.get(index));
        } catch(IndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }
}
