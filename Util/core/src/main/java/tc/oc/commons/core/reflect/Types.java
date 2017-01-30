package tc.oc.commons.core.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import com.google.inject.TypeLiteral;
import tc.oc.commons.core.util.ArrayUtils;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class Types {
    private Types() {}

    /**
     * Convert a Guava {@link TypeToken} to a Guice {@link TypeLiteral}
     */
    public static <T> TypeLiteral<T> toLiteral(TypeToken<T> typeToken) {
        return (TypeLiteral<T>) TypeLiteral.get(typeToken.getType());
    }

    /**
     * Convert a Guice {@link TypeLiteral} to a Guava {@link TypeToken}
     */
    public static <T> TypeToken<T> toToken(TypeLiteral<T> typeLiteral) {
        return (TypeToken<T>) TypeToken.of(typeLiteral.getType());
    }

    public static <T, P> TypeLiteral<T> resolve(TypeLiteral<T> type, TypeParameter<P> parameter, Class<P> argument) {
        return toLiteral(toToken(type).where(parameter, argument));
    }

    public static <T, P> TypeLiteral<T> resolve(TypeLiteral<T> type, TypeParameter<P> parameter, TypeLiteral<P> argument) {
        return toLiteral(toToken(type).where(parameter, toToken(argument)));
    }

    public static <T> TypeLiteral<T> resolve(TypeLiteral<T> type, Class<?> declaringClass) {
        return (TypeLiteral<T>) toLiteral(TypeToken.of(declaringClass).resolveType(type.getType()));
    }

    public static boolean isAssignable(Class<?> to, TypeToken<?> from) {
        return to.isAssignableFrom(from.getRawType());
    }

    public static boolean isAssignable(Class<?> to, TypeLiteral<?> from) {
        return to.isAssignableFrom(from.getRawType());
    }

    public static boolean isAssignable(TypeLiteral<?> to, TypeLiteral<?> from) {
        return toToken(to).isAssignableFrom(from.getType());
    }

    public static boolean isAssignable(Type to, Type from) {
        return TypeToken.of(to).isAssignableFrom(from);
    }

    private static final ImmutableMap<Class<?>, Class<?>> PRIMITIVE_PROMOTIONS = ImmutableMap
        .<Class<?>, Class<?>>builder()
        .put(byte.class, short.class)
        .put(short.class, int.class)
        .put(char.class, int.class)
        .put(int.class, long.class)
        .put(long.class, float.class)
        .put(float.class, double.class)
        .build();

    public static boolean isPromotable(Class<?> to, @Nullable Class<?> from) {
        return from != null && (to.equals(from) || isPromotable(to, PRIMITIVE_PROMOTIONS.get(from)));
    }

    private static final ImmutableBiMap<Class<?>, Class<?>> BOXINGS;
    static {
        BOXINGS = ImmutableBiMap.<Class<?>, Class<?>>builder()
            .put(boolean.class, Boolean.class)
            .put(char.class, Character.class)
            .put(byte.class, Byte.class)
            .put(short.class, Short.class)
            .put(int.class, Integer.class)
            .put(long.class, Long.class)
            .put(float.class, Float.class)
            .put(double.class, Double.class)
            .build();
    }

    public static <T> Class<T> box(Class<T> type) {
        return type.isPrimitive() ? (Class<T>) BOXINGS.get(type) : type;
    }

    public static <T> TypeToken<T> box(TypeToken<T> type) {
        return type.isPrimitive() ? TypeToken.of(box((Class<T>) type.getRawType())) : type;
    }

    public static <T> Class<T> unbox(Class<T> type) {
        if(type.isPrimitive()) return type;
        type = (Class<T>) BOXINGS.inverse().get(type);
        if(type == null) throw new IllegalArgumentException(type.getName() + " is not a primitive type");
        return type;
    }

    /**
     * Test if an invocation conversion can be applied to the given types.
     *
     * https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.3
     */
    public static boolean isConvertibleForInvocation(TypeToken<?> to, TypeToken<?> from) {
        if(to.isPrimitive()) {
            // Assigning to a primitive allows for both unboxing and primitive widening
            Class<?> fromRaw = from.getRawType();
            if(!fromRaw.isPrimitive()) {
                fromRaw = BOXINGS.inverse().get(fromRaw);
                if(fromRaw == null) return false;
            }
            return isPromotable(to.getRawType(), fromRaw);
        } else if(from.isPrimitive()) {
            // Assigning to an object from a primitive allows boxing and reference widening
            return to.isAssignableFrom(box(from.getRawType()));
        } else {
            return to.isAssignableFrom(from);
        }
    }

    public static boolean isConvertibleForInvocation(Type to, Type from) {
        return isConvertibleForInvocation(TypeToken.of(to), TypeToken.of(from));
    }

    public static boolean isFullySpecified(Type type) {
        checkNotNull(type);
        if(type instanceof Class) {
            return true;
        } else if(type instanceof TypeVariable) {
            return false;
        } else if(type instanceof GenericArrayType) {
            return isFullySpecified(((GenericArrayType) type).getGenericComponentType());
        } else if(type instanceof WildcardType) {
            final WildcardType wildcard = (WildcardType) type;
            return Stream.of(wildcard.getLowerBounds()).allMatch(Types::isFullySpecified) &&
                   Stream.of(wildcard.getUpperBounds()).allMatch(Types::isFullySpecified);
        } else if(type instanceof ParameterizedType) {
            final ParameterizedType parameterized = (ParameterizedType) type;
            return isFullySpecified(parameterized.getRawType()) &&
                   (parameterized.getOwnerType() == null || isFullySpecified(parameterized.getOwnerType())) &&
                   Stream.of(parameterized.getActualTypeArguments()).allMatch(Types::isFullySpecified);
        } else {
            throw new IllegalArgumentException("Unhandled metatype " + type.getClass());
        }
    }

    public static boolean isFullySpecified(TypeToken<?> type) {
        return isFullySpecified(type.getType());
    }

    public static Type assertFullySpecified(Type type) {
        if(!isFullySpecified(type)) {
            throw new IllegalArgumentException("Type " + type + " is not fully specified");
        }
        return type;
    }

    public static <T> TypeLiteral<T> assertFullySpecified(TypeLiteral<T> type) {
        assertFullySpecified(type.getType());
        return type;
    }

    public static <T> TypeToken<T> assertFullySpecified(TypeToken<T> type) {
        assertFullySpecified(type.getType());
        return type;
    }

    public static <D extends GenericDeclaration> TypeVariable<D> typeVariable(D decl, String name) {
        for(TypeVariable<?> var : decl.getTypeParameters()) {
            if(name.equals(var.getName())) {
                return (TypeVariable<D>) var;
            }
        }
        throw new IllegalArgumentException(decl + " has no type parameter named '" + name + "'");
    }

    public static TypeToken<?> actualTypeArgument(TypeToken<?> parameterizedType, int position) {
        return actualTypeArgument(parameterizedType.getType(), position);
    }

    public static TypeToken<?> actualTypeArgument(Type parameterizedType, int position) {
        checkArgument(parameterizedType instanceof ParameterizedType);
        return TypeToken.of(((ParameterizedType) parameterizedType).getActualTypeArguments()[position]);
    }

    public static @Nullable Type superclass(Type type) {
        return type instanceof Class ? ((Class) type).getGenericSuperclass() : null;
    }

    /**
     * Get all superclasses of the given type, including the type itself.
     * If the given type is an interface, it will be the only element
     * in the result.
     */
    public static <T> List<Class<? super T>> superclasses(Class<T> type) {
        ImmutableList.Builder<Class<? super T>> list = ImmutableList.builder();
        for(Class<?> sup = type; sup != null; sup = sup.getSuperclass()) {
            list.add((Class<? super T>) sup);
        }
        return list.build();
    }

    public static List<Type> interfaces(Type type) {
        return type instanceof Class ? Arrays.asList(((Class) type).getGenericInterfaces())
                                     : Collections.emptyList();
    }

    public static <T> List<Class<? super T>> interfaces(Class<T> type) {
        return Arrays.asList((Class<? super T>[]) type.getInterfaces());
    }

    public static <T> Set<Class<? super T>> minimalInheritedInterfaces(Class<T> type) {
        if(type.isInterface()) {
            return ImmutableSet.of(type);
        }

        final MinimalSupertypeSet<T> interfaces = new MinimalSupertypeSet<>();
        for(Class<? super T> supertype = type; supertype != null; supertype = supertype.getSuperclass()) {
            for(Class<?> iface : supertype.getInterfaces()) {
                interfaces.add(iface);
            }
        }
        return interfaces.toClassSet();
    }

    /**
     * Get the immediate parent types of the given class i.e. the
     * direct superclass (if any) and any directly implemented interfaces.
     */
    public static <T> Iterable<Class<? super T>> parents(Class<T> type) {
        Class<? super T> superclass = type.getSuperclass();
        Class<? super T>[] interfaces = (Class<? super T>[]) type.getInterfaces();
        if(superclass == null) {
            return Arrays.asList(interfaces);
        } else {
            return Iterables.concat(Arrays.asList(interfaces), Collections.singleton(superclass));
        }
    }

    /**
     * Get all ancestor types of the given type, both classes and interfaces,
     * in breadth-first order. The superclass of each class is traversed before
     * its interfaces.
     *
     * @param allowDuplicates If true, duplicate interfaces will appear in the result wherever
     *                        they occur in the ancestry graph. If false, all but the
     *                        first occurance of each interface will be omitted from the result.
     *                        This makes the operation somewhat more expensive, so duplicates
     *                        should be allowed if possible.
     */
    public static <T> Collection<Class<? super T>> ancestors(Class<T> type, boolean allowDuplicates) {
        List<Class<? super T>> list = new ArrayList<>();
        list.add(type);
        for(int i = 0; i < list.size(); i++) {
            final Class<?> t = list.get(i);
            if(t.getSuperclass() != null) list.add((Class<? super T>) t.getSuperclass());
            final Class<? super T>[] interfaces = (Class<? super T>[]) t.getInterfaces();
            if(allowDuplicates) {
                Collections.addAll(list, interfaces);
            } else {
                for(Class<? super T> iface : interfaces) {
                    if(!list.contains(iface)) list.add(iface);
                }
            }
        }
        return list;
    }

    /**
     * Equivalent to {@link #ancestors(Class, boolean)} with duplicate results allowed.
     */
    public static <T> Collection<Class<? super T>> ancestors(Class<T> type) {
        return ancestors(type, true);
    }

    /**
     * Traverse ancestors of the given type, in the same order as {@link #ancestors(Class, boolean)},
     * and return the first ancestor for which the given predicate returns true.
     */
    public static @Nullable <U> Class<? extends U> findAncestor(Class<? extends U> type, Class<U> upperBound, java.util.function.Predicate<Class<?>> pred) {
        Deque<Class<? extends U>> queue = new ArrayDeque<>();
        queue.add(type);
        while(!queue.isEmpty()) {
            final Class<? extends U> t = queue.remove();
            if(pred.test(t)) return t;

            if(t.getSuperclass() != null && upperBound.isAssignableFrom(t.getSuperclass())) {
                queue.add((Class<? extends U>) t.getSuperclass());
            }

            for(Class<?> iface : t.getInterfaces()) {
                if(upperBound.isAssignableFrom(iface)) {
                    queue.add((Class<? extends U>) iface);
                }
            }
        }
        return null;
    }

    public static @Nullable Class<?> findAncestor(Class<?> type, java.util.function.Predicate<Class<?>> pred) {
        Deque<Class<?>> queue = new ArrayDeque<>();
        queue.add(type);
        while(!queue.isEmpty()) {
            final Class<?> t = queue.remove();
            if(pred.test(t)) return t;

            if(t.getSuperclass() != null) {
                queue.add(t.getSuperclass());
            }

            Collections.addAll(queue, t.getInterfaces());
        }
        return null;
    }

    /**
     * Traverse ancestors of the given type, in the same order as {@link #ancestors(Class, boolean)},
     * applying the given function to each ancestor, and return the first non-null result, or null
     * if the function returns null for all ancestors.
     */
    public static @Nullable <T, R> R findForAncestor(Class<T> type, Function<Class<? super T>, R> func) {
        Deque<Class<? super T>> queue = new ArrayDeque<>();
        queue.add(type);
        while(!queue.isEmpty()) {
            final Class<? super T> t = queue.remove();
            final R result = func.apply(t);
            if(result != null) return result;
            if(t.getSuperclass() != null) queue.add(t.getSuperclass());
            Collections.addAll(queue, (Class<? super T>[]) t.getInterfaces());
        }
        return null;
    }

    public static String externalToInternal(String name) {
        return name.replace('.', '/');
    }

    public static String internalToExternal(String name) {
        return name.replace('/', '.');
    }

    public static String descriptor(Class<?> type) {
        if(type.isPrimitive()) {
            if(type == byte.class) return "B";
            if(type == char.class) return "C";
            if(type == double.class) return "D";
            if(type == float.class) return "F";
            if(type == int.class) return "I";
            if(type == long.class) return "J";
            if(type == short.class) return "S";
            if(type == boolean.class) return "Z";
            if(type == void.class) return "V";
            throw new RuntimeException("Unrecognized primitive " + type);
        }
        String desc = type.isArray() ? type.getName() // Array type names already have "L...;"
                                  : 'L' + type.getName() + ';';
        return externalToInternal(desc);
    }

    /**
     * Get the inner class of the given parent with the given name.
     * The name can be fully qualified, or "simple" i.e. just the member name.
     * The numeric names of anonymous classes will also work.
     */
    public static @Nullable Class<?> getDeclaredClass(Class<?> parent, String name) {
        if(name.indexOf('$') == -1) {
            name = parent.getName() + '$' + name;
        }
        for(Class<?> type : parent.getDeclaredClasses()) {
            if(name.equals(type.getName())) return type;
        }
        return null;
    }

    public static Class<?> needDeclaredClass(Class<?> parent, String name) throws ClassNotFoundException {
        final Class<?> type = getDeclaredClass(parent, name);
        if(type == null) {
            throw new ClassNotFoundException("No inner class with name '" + name + "' in type '" + parent.getName() + "'");
        }
        return type;
    }

    public static @Nullable <T extends Annotation> T inheritableAnnotation(Class<?> cls, Class<T> annotation) {
        for(Class<?> anc : ancestors(cls)) {
            T annot = anc.getAnnotation(annotation);
            if(annot != null) return annot;
        }
        return null;
    }

    public static boolean instanceOfAny(Object o, Class... types) {
        for(Class type : types) {
            if(type.isInstance(o)) return true;
        }
        return false;
    }

    public static boolean instanceOfAll(Object o, Class... types) {
        for(Class type : types) {
            if(!type.isInstance(o)) return false;
        }
        return true;
    }

    public static Predicate<? super Type> assignableFrom(final Type from) {
        return to -> to != null && TypeToken.of(to).isAssignableFrom(from);
    }

    public static Predicate<? super Type> assignableTo(final Type to) {
        final TypeToken<?> toToken = TypeToken.of(to);
        return from -> from != null && toToken.isAssignableFrom(from);
    }

    public static <V extends ReflectionVisitor> V walkAncestors(Class<?> cls, V visitor) {
        return walkAncestors(cls, null, visitor);
    }

    public static <V extends ReflectionVisitor> V walkAncestors(Class<?> cls, @Nullable Predicate<? super Class<?>> filter, V visitor) {
        if(filter != null && !filter.test(cls)) return visitor;
        if(!visitor.visit(cls)) return visitor;

        for(Method method : cls.getDeclaredMethods()) {
            visitor.visit(cls, method);
        }

        for(Field field : cls.getDeclaredFields()) {
            visitor.visit(cls, field);
        }

        Class<?> superclass = cls.getSuperclass();
        if(superclass != null) {
            walkAncestors(superclass, filter, visitor);
        }

        for(Class<?> iface : cls.getInterfaces()) {
            walkAncestors(iface, filter, visitor);
        }

        return visitor;
    }

    public static @Nullable <T> Optional<Class<? extends T>> commonAncestor(Class<T> base, Stream<Class<?>> types) {
        return types
            .reduce((a, b) -> a.isAssignableFrom(b) ? a : b)
            .map(t -> t.asSubclass(base));
    }

    public static TypeToken parameterizedTypeToken(TypeToken<?> owner, TypeToken<?>... args) {
        return parameterizedTypeToken(owner.getType(), args);
    }

    public static TypeToken parameterizedTypeToken(Type owner, TypeToken<?>... args) {
        return parameterizedTypeToken(owner, ArrayUtils.transform(args, new Type[args.length], TypeToken::getType));
    }

    public static TypeToken parameterizedTypeToken(Type owner, Type... args) {
        return TypeToken.of(com.google.inject.util.Types.newParameterizedType(owner, (Type[]) args));
    }

    public static TypeLiteral parameterizedTypeLiteral(TypeLiteral<?> owner, TypeLiteral<?>... args) {
        return parameterizedTypeLiteral(owner.getType(), args);
    }

    public static TypeLiteral parameterizedTypeLiteral(Type owner, TypeLiteral<?>... args) {
        return parameterizedTypeLiteral(owner, ArrayUtils.transform(args, new Type[args.length], TypeLiteral::getType));
    }

    public static TypeLiteral parameterizedTypeLiteral(Type owner, Type... args) {
        return TypeLiteral.get(com.google.inject.util.Types.newParameterizedType(owner, (Type[]) args));
    }

    public static <T> TypeToken<Set<T>> setOf(TypeToken<T> elementType) {
        return new TypeToken<Set<T>>(){}.where(new TypeParameter<T>(){}, elementType);
    }

    public static <T> TypeLiteral<Set<T>> setOf(TypeLiteral<T> elementType) {
        return toLiteral(setOf(toToken(elementType)));
    }

    public static <T> TypeToken<List<T>> listOf(TypeToken<T> elementType) {
        return new TypeToken<List<T>>(){}.where(new TypeParameter<T>(){}, elementType);
    }

    public static <T> TypeLiteral<List<T>> listOf(TypeLiteral<T> elementType) {
        return toLiteral(listOf(toToken(elementType)));
    }
}
