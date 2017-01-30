package tc.oc.commons.core.inject;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.inject.Qualifier;

import com.google.common.cache.LoadingCache;
import com.google.common.reflect.Invokable;
import com.google.common.reflect.TypeToken;
import com.google.inject.BindingAnnotation;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.internal.Annotations;
import com.google.inject.internal.Errors;
import com.google.inject.internal.ErrorsException;
import tc.oc.commons.core.reflect.Types;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.commons.core.util.CacheUtils;
import tc.oc.commons.core.util.Optionals;

public final class Keys {
    private Keys() {}

    public static Key<?> fieldType(TypeLiteral<?> owner, Field field, Errors errors) throws ErrorsException {
        return Annotations.getKey(owner.getFieldType(field), field, field.getAnnotations(), errors);
    }

    public static <T> Key<T> returnType(Invokable<?, T> method, Errors errors) throws ErrorsException {
        return (Key<T>) Annotations.getKey(Types.toLiteral(method.getReturnType()), method, method.getAnnotations(), errors);
    }

    public static <T> Key<T> returnType(TypeLiteral<?> decl, Method method, Errors errors) throws ErrorsException {
        return (Key<T>) Annotations.getKey(decl.getReturnType(method), method, method.getAnnotations(), errors);
    }

    public static <T> Key<T> get(TypeLiteral<T> type, @Nullable Annotation annotation) {
        return annotation == null ? Key.get(type) : Key.get(type, annotation);
    }

    public static <T> Key<T> get(Key<T> type, @Nullable Annotation annotation) {
        return get(type.getTypeLiteral(), annotation);
    }

    public static Set<Key<?>> get(Class<?>... types) {
        return Stream.of(types).map(Key::get).collect(Collectors.toImmutableSet());
    }

    public static <T> Key<T> get(TypeToken<T> type) {
        return Key.get(Types.toLiteral(type));
    }

    public static <T> Key<T> forInstance(T instance) {
        return forInstance((Class<T>) instance.getClass(), instance);
    }

    public static <T> Key<T> forInstance(Class<T> type, T instance) {
        return forInstance(TypeLiteral.get(type), instance);
    }

    public static <T> Key<T> forInstance(TypeLiteral<T> type, T instance) {
        return Key.get(type, new InstanceQualifierImpl<>(instance));
    }

    public static <T> Key<Set<T>> setOf(Key<T> key) {
        return get(Types.setOf(key.getTypeLiteral()), key.getAnnotation());
    }

    public static <T> Key<List<T>> listOf(Key<T> key) {
        return get(Types.listOf(key.getTypeLiteral()), key.getAnnotation());
    }

    private static final LoadingCache<TypeLiteral, Key> OPTIONAL_BY_TYPE_LITERAL = CacheUtils.newCache(
        type -> Key.get(Optionals.optionalType(type))
    );

    private static final LoadingCache<Type, Key> OPTIONAL_BY_TYPE = CacheUtils.newCache(
        type -> OPTIONAL_BY_TYPE_LITERAL.get(TypeLiteral.get(type))
    );

    private static final LoadingCache<Key, Key> OPTIONAL_BY_KEY = CacheUtils.newCache(
        key -> OPTIONAL_BY_TYPE_LITERAL.get(key.getTypeLiteral())
    );

    public static <T> Key<Optional<T>> optional(TypeLiteral<T> type) {
        return OPTIONAL_BY_TYPE_LITERAL.getUnchecked(type);
    }

    public static <T> Key<Optional<T>> optional(Class<T> type) {
        return OPTIONAL_BY_TYPE.getUnchecked(type);
    }

    public static <T> Key<Optional<T>> optional(Key<T> key) {
        return OPTIONAL_BY_KEY.getUnchecked(key);
    }

    public static Stream<Key<Optional<?>>> optional(Stream<Key<?>> keys) {
        return keys.map(OPTIONAL_BY_KEY::getUnchecked);
    }

    public static Set<Key<Optional<?>>> optional(Class<?>... types) {
        return (Set) Stream.of(types).map(Keys::optional).collect(Collectors.toImmutableSet());
    }
}

@Qualifier @BindingAnnotation @Retention(RetentionPolicy.RUNTIME)
@interface InstanceQualifier {
    boolean isWaterWet();
}

class InstanceQualifierImpl<T> implements InstanceQualifier {
    final T instance;

    InstanceQualifierImpl(T instance) {
        this.instance = instance;
    }

    @Override
    public int hashCode() {
        return instance.hashCode();
    }

    @Override
    public boolean equals(Object that) {
        return this == that || (
            that instanceof InstanceQualifierImpl &&
            instance.equals(((InstanceQualifierImpl) that).instance)
        );
    }

    @Override
    public boolean isWaterWet() {
        return true;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return InstanceQualifier.class;
    }
}