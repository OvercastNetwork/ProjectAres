package tc.oc.commons.core.util;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import com.google.inject.TypeLiteral;
import tc.oc.commons.core.reflect.Methods;
import tc.oc.commons.core.reflect.Types;

public final class Optionals {
    private Optionals() {}

    public static boolean equals(Optional<?> a, Optional<?> b) {
        return a.isPresent() && b.isPresent() && a.get().equals(b.get());
    }

    public static boolean equals(Optional<?> a, Object b) {
        return a.isPresent() && a.get().equals(b);
    }

    public static boolean equals(Object a, Optional<?> b) {
        return equals(b, a);
    }

    public static boolean isInstance(Optional<?> value, Class<?> type) {
        return value.isPresent() && type.isInstance(value.get());
    }

    public static <T> Optional<T> cast(Optional<? super T> value, Class<T> type) {
        return isInstance(value, type) ? (Optional<T>) value : Optional.empty();
    }

    public static <T> Optional<T> cast(@Nullable Object obj, Class<T> type) {
        return type.isInstance(obj) ? Optional.ofNullable((T) obj)
                                    : Optional.empty();
    }

    public static <T> Optional<T> getIf(boolean condition, Supplier<T> supplier) {
        return condition ? Optional.of(supplier.get())
                         : Optional.empty();
    }

    public static <T, E extends Throwable> Optional<T> ofThrows(Class<E> exception, ThrowingSupplier<T, E> supplier) {
        try {
            return Optional.of(supplier.getThrows());
        } catch(Throwable e) {
            if(exception.isInstance(e)) {
                return Optional.empty();
            }
            throw (RuntimeException) e;
        }
    }

    public static <T> Optional<T> filter(T value, Predicate<? super T> filter) {
        return Optional.of(value).filter(filter);
    }

    public static boolean contains(Optional<?> container, Object value) {
        return container.isPresent() && container.get().equals(value);
    }

    public static <T> Set<T> toSet(Optional<T> t) {
        return t.isPresent() ? Collections.singleton(t.get())
                             : Collections.emptySet();
    }

    public static <T> Stream<T> stream(Optional<T> t) {
        return t.map(Stream::of).orElse(Stream.empty());
    }

    public static <T> Set<T> union(Stream<Optional<? extends T>> optionals) {
        return optionals.filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toSet());
    }

    public static <T> Set<T> union(Collection<Optional<? extends T>> optionals) {
        return union(optionals.stream());
    }

    public static <T> Set<T> union(Optional<? extends T>... optionals) {
        return union(Stream.of(optionals));
    }

    public static <T> Optional<T> first(Iterable<T> iterable) {
        final Iterator<T> iterator = iterable.iterator();
        return iterator.hasNext() ? Optional.of(iterator.next()) : Optional.empty();
    }

    public static <T> Optional<T> first(Stream<Optional<? extends T>> options) {
        return (Optional<T>) options.filter(Optional::isPresent)
                                    .findFirst()
                                    .orElse(Optional.empty());
    }

    public static <T> Optional<T> first(Collection<Optional<? extends T>> options) {
        return first(options.stream());
    }

    public static <T> Optional<T> first(Optional<? extends T>... options) {
        return first(Stream.of(options));
    }

    public static <T> TypeToken<Optional<T>> optionalType(TypeToken<T> type) {
        return new TypeToken<Optional<T>>(){}.where(new TypeParameter<T>(){}, type);
    }

    public static <T> TypeLiteral<Optional<T>> optionalType(TypeLiteral<T> type) {
        return Types.toLiteral(optionalType(Types.toToken(type)));
    }

    public static <T> TypeToken<Optional<? extends T>> optionalSubtypes(TypeToken<T> type) {
        return new TypeToken<Optional<? extends T>>(){}.where(new TypeParameter<T>(){}, type);
    }

    public static <T> TypeLiteral<Optional<? extends T>> optionalSubtypes(TypeLiteral<T> type) {
        return Types.toLiteral(optionalSubtypes(Types.toToken(type)));
    }

    private static final Method GET_METHOD = Methods.method(Optional.class, "get");

    public static <T> TypeToken<T> elementType(TypeToken<Optional<T>> optionalType) {
        return (TypeToken<T>) optionalType.method(GET_METHOD).getReturnType();
    }

    public static <T> TypeLiteral<T> elementType(TypeLiteral<Optional<T>> optionalType) {
        return (TypeLiteral<T>) optionalType.getReturnType(GET_METHOD);
    }

    public static <T> TypeToken<T> elementType(Type optionalType) {
        return elementType((TypeToken<Optional<T>>) TypeToken.of(optionalType));
    }

    /**
     * If the given {@link Optional} is present, return the result of combining its value
     * with the identity value, using the given combiner function. Otherwise, return the
     * identity value directly.
     */
    public static <T, R> R reduce(R identity, Optional<T> optional, BiFunction<? super R, ? super T, ? extends R> combiner) {
        return optional.<R>map(value -> combiner.apply(identity, value))
                       .orElse(identity);
    }

    public static <T, U> Optional<Pair<T, U>> both(Optional<T> t, Optional<U> u) {
        return mapBoth(t, u, Pair::of);
    }

    public static <T, U, R> Optional<R> mapBoth(Optional<T> t, Optional<U> u, BiFunction<T, U, R> mapper) {
        return flatMapBoth(t, u, mapper.andThen(Optional::of));
    }

    public static <T, U, R> Optional<R> flatMapBoth(Optional<T> t, Optional<U> u, BiFunction<T, U, Optional<R>> mapper) {
        return t.isPresent() && u.isPresent() ? mapper.apply(t.get(), u.get())
                                              : Optional.empty();
    }
}
