package tc.oc.commons.core.reflect;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.inject.TypeLiteral;

public interface TypeLiterals {

    default <T> TypeLiteral<Optional<T>> Optional(TypeLiteral<T> type) {
        return new ResolvableType<Optional<T>>(){}.with(new TypeArgument<T>(type){});
    }

    default <T> TypeLiteral<Optional<T>> Optional(Class<T> type) {
        return Optional(TypeLiteral.get(type));
    }

    default <T> TypeLiteral<Iterable<T>> Iterable(TypeLiteral<T> type) {
        return new ResolvableType<Iterable<T>>(){}.with(new TypeArgument<T>(type){});
    }

    default <T> TypeLiteral<Iterable<T>> Iterable(Class<T> type) {
        return Iterable(TypeLiteral.get(type));
    }

    default <T> TypeLiteral<Collection<T>> Collection(TypeLiteral<T> type) {
        return new ResolvableType<Collection<T>>(){}.with(new TypeArgument<T>(type){});
    }

    default <T> TypeLiteral<Collection<T>> Collection(Class<T> type) {
        return Collection(TypeLiteral.get(type));
    }

    default <T> TypeLiteral<Set<T>> Set(TypeLiteral<T> type) {
        return new ResolvableType<Set<T>>(){}.with(new TypeArgument<T>(type){});
    }

    default <T> TypeLiteral<Set<T>> Set(Class<T> type) {
        return Set(TypeLiteral.get(type));
    }

    default <T> TypeLiteral<List<T>> List(TypeLiteral<T> type) {
        return new ResolvableType<List<T>>(){}.with(new TypeArgument<T>(type){});
    }

    default <T> TypeLiteral<List<T>> List(Class<T> type) {
        return List(TypeLiteral.get(type));
    }

    default <K, V> TypeLiteral<Map<K, V>> Map(TypeLiteral<K> key, TypeLiteral<V> value) {
        return new ResolvableType<Map<K, V>>(){}.with(new TypeArgument<K>(key){},
                                                      new TypeArgument<V>(value){});
    }

    default <K, V> TypeLiteral<Map<K, V>> Map(Class<K> key, Class<V> value) {
        return Map(TypeLiteral.get(key), TypeLiteral.get(value));
    }
}
