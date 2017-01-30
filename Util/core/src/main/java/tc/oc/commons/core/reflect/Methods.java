package tc.oc.commons.core.reflect;

import java.lang.annotation.Annotation;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.Invokable;
import com.google.common.reflect.Parameter;
import com.google.common.reflect.TypeToken;

import static com.google.common.base.Preconditions.checkArgument;

public final class Methods {
    private Methods() {}

    public static String describeParameters(Stream<Class<?>> parameterTypes) {
        return "(" + parameterTypes.map(Class::getSimpleName).collect(Collectors.joining(", ")) + ")";
    }

    public static String describeParameters(Class<?>... parameterTypes) {
        return describeParameters(Stream.of(parameterTypes));
    }

    public static String describeParameters(MethodType methodType) {
        return describeParameters(methodType.parameterList().stream());
    }

    public static String describe(@Nullable Class<?> decl, @Nullable Class<?> returnType, @Nullable String name, @Nullable Stream<Class<?>> parameterTypes) {
        String text = "";
        if(returnType != null) {
            text += returnType.getSimpleName() + " ";
        }
        if(name != null) {
            if(decl != null) {
                text += decl.getSimpleName() + "#" + name;
            } else {
                text += name;
            }
        }
        if(parameterTypes != null) {
            text += describeParameters(parameterTypes);
        }
        return text;
    }

    public static String describe(Class<?> decl, Class<?> returnType, String name, Class<?>... parameterTypes) {
        return describe(decl, returnType, name, Stream.of(parameterTypes));
    }

    public static String describe(Class<?> decl, String name, Class<?>... parameterTypes) {
        return describe(decl, null, name, Stream.of(parameterTypes));
    }

    public static String describe(Class<?> decl, String name) {
        return describe(decl, null, name, (Stream) null);
    }

    public static String describe(Method method) {
        return describe(method.getDeclaringClass(), method.getReturnType(), method.getName(), method.getParameterTypes());
    }

    public static String describe(Class<?> decl, MethodType methodType, String name) {
        return describe(decl, methodType.returnType(), name, methodType.parameterArray());
    }

    public static String describe(MethodType methodType, String name) {
        return describe(null, methodType, name);
    }

    public static String removeBeanPrefix(String name, String prefix) {
        if(name.startsWith(prefix) && name.length() > prefix.length()) {
            final char first = name.charAt(prefix.length());
            if(Character.isUpperCase(first)) {
                return Character.toLowerCase(first) + name.substring(prefix.length() + 1);
            }
        }
        return name;
    }

    public static String removeBeanPrefix(String name) {
        name = removeBeanPrefix(name, "get");
        name = removeBeanPrefix(name, "set");
        name = removeBeanPrefix(name, "is");
        return name;
    }

    public static @Nullable Method tryMethod(Class<?> decl, Method method) {
        return tryMethod(decl, method.getName(), method.getParameterTypes());
    }

    public static @Nullable Method tryMethod(Class<?> decl, String name, Class<?>... params) {
        try {
            return decl.getMethod(name, params);
        } catch(NoSuchMethodException e) {
            return null;
        }
    }

    public static @Nullable Method tryDeclaredMethod(Class<?> decl, String name, Class<?>... params) {
        try {
            return decl.getDeclaredMethod(name, params);
        } catch(NoSuchMethodException e) {
            return null;
        }
    }

    public static boolean hasMethod(Class<?> decl, String name, Class<?>... params) {
        return tryMethod(decl, name, params) != null;
    }

    public static Method method(Class<?> decl, Method method) {
        return method(decl, method.getName(), method.getParameterTypes());
    }

    public static Method method(Class<?> decl, String name, Class<?>... params) {
        try {
            return decl.getMethod(name, params);
        } catch(NoSuchMethodException e) {
            throw new NoSuchMethodError(describe(decl, name, params));
        }
    }

    public static Method declaredMethod(Class<?> decl, String name, Class<?>... params) {
        try {
            return decl.getDeclaredMethod(name, params);
        } catch(NoSuchMethodException e) {
            throw new NoSuchMethodError(describe(decl, name, params));
        }
    }

    public static void assertPublicThrows(Executable method, Class<?>... exceptions) {
        Members.assertPublic(method);

        for(Class<?> ex : method.getExceptionTypes()) {
            if(!RuntimeException.class.isAssignableFrom(ex)) {
                boolean found = false;
                for(Class<?> allowed : exceptions) {
                    if(allowed.isAssignableFrom(ex)) {
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    Members.error(method, "throws unhandled exception " + ex.getName());
                }
            }
        }
    }

    public static String descriptor(Class<?>[] parameterTypes, Class<?> returnType) {
        String desc = "(";
        for(Class param : parameterTypes) {
            desc += Types.descriptor(param);
        }
        return desc + ')' + Types.descriptor(returnType);
    }

    public static String descriptor(Method method) {
        return descriptor(method.getParameterTypes(), method.getReturnType());
    }

    public static String descriptor(Constructor<?> ctor) {
        return descriptor(ctor.getParameterTypes(), void.class);
    }

    public static boolean isCallable(Method method) {
        return !Members.isAbstract(method);
    }

    public static boolean respondsTo(Object obj, Method call) {
        return call.getDeclaringClass().isInstance(obj) || respondsTo(obj.getClass(), call);
    }

    public static boolean respondsTo(Class<?> cls, Method call) {
        return callableMethod(cls, call) != null;
    }

    public static @Nullable Method accessibleMethod(Class<?> cls, Method call) {
        Method method = findClassMethod(cls, call);
        if(method != null) return method;
        return findInterfaceMethod(cls, call, false);
    }

    public static @Nullable Method callableMethod(Class<?> cls, Method call) {
        Method method = findClassMethod(cls, call);
        if(method != null) {
            // If any superclass declares the method, defaults will not
            // be called, even if the override is abstract.
            return isCallable(method) ? method : null;
        }

        method = findInterfaceMethod(cls, call, true);
        if(method != null) return method;

        return null;
    }

    private static @Nullable Method findClassMethod(@Nullable Class<?> cls, Method call) {
        if(cls == null) return null;
        if(cls.isInterface()) return null;

        try {
            final Method method = cls.getDeclaredMethod(call.getName(), call.getParameterTypes());
            if(!Members.isPrivate(method)) return method; // may be abstract
        } catch(NoSuchMethodException ignored) {}

        return findClassMethod(cls.getSuperclass(), call);
    }

    private static @Nullable Method findInterfaceMethod(@Nullable Class<?> cls, Method call, boolean callable) {
        if(cls == null) return null;
        if(callable && call.getDeclaringClass().isAssignableFrom(Object.class)) return null; // Object method defaults are not allowed

        if(cls.isInterface()) {
            try {
                final Method method = cls.getDeclaredMethod(call.getName(), call.getParameterTypes());
                if(!callable || isCallable(method)) return method;
            } catch(NoSuchMethodException ignored) {}
        }

        for(Class<?> iface : cls.getInterfaces()) {
            final Method method = findInterfaceMethod(iface, call, callable);
            if(method != null) return method;
        }

        return null;
    }

    /**
     * Would the given method be overridden/implemented by a method with the given name and parameter types?
     *
     * This is true if all of the following are true:
     *
     *     - The parent method is overridable (i.e. non-private, and non-static)
     *     - Both methods have the same name
     *     - Both methods have identical parameter types
     *     - The parent method's return type is assignable from the child method's return type
     *
     * @param parent            Parent method
     * @param returnType        Return type of the child method
     * @param name              Name of the child method
     * @param parameterTypes    Parameter types of the child method
     */
    //public static boolean isOverride(Invokable parent, TypeToken<?> returnType, String name, List<TypeToken<?>> parameterTypes) {
    //    return name.equals(parent.getName()) &&
    //           isSignatureOverride(parent, returnType, parameterTypes);
    //}
    //
    //public static boolean isSignatureOverride(Invokable parent, TypeToken<?> returnType, List<TypeToken<?>> parameterTypes) {
    //    return parent.isOverridable() &&
    //           parent.getParameters()
    //           parameterTypes.equals(context.getParameterTypes(parent)) &&
    //           Types.isAssignable(context.getReturnType(parent), returnType);
    //}
    //
    //public static boolean isOverride(Method parent, Invokable child, TypeLiteral<?> context) {
    //    return child.getName().equals(parent.getName()) &&
    //           isSignatureOverride(parent, child, context);
    //}
    //
    //public static boolean isSignatureOverride(Invokable parent, Invokable child) {
    //    return parent.isOverridable() &&
    //           parent.getParameters()
    //    return isSignatureOverride(parent,
    //                               context.getReturnType(child),
    //                               context.getParameterTypes(child),
    //                               context);
    //}

    /**
     * Find a method declared on the given class that would override, or be overridden by,
     * a method with the given name and parameter types. Return null if no such method can be found.
     */
    public static @Nullable Method overrideIn(Class<?> cls, String name, Class<?>... parameterTypes) {
        try {
            final Method method = cls.getDeclaredMethod(name, parameterTypes);
            if(Members.isInheritable(method)) return method;
        } catch(NoSuchMethodException ignored) {}
        return null;
    }

    public static @Nullable Method overrideIn(Class<?> cls, Method child) {
        return overrideIn(cls, child.getName(), child.getParameterTypes());
    }

    public static boolean hasOverrideIn(Class<?> cls, Method method) {
        return overrideIn(cls, method) != null;
    }

    public static Set<Method> ancestors(Method method) {
        if(Members.isPrivate(method)) return Collections.emptySet();

        final ImmutableSet.Builder<Method> builder = ImmutableSet.builder();
        for(Class<?> ancestor : Types.ancestors(method.getDeclaringClass())) {
            final Method sup = overrideIn(ancestor, method);
            if(sup != null) builder.add(sup);
        }
        return builder.build();
    }

    public static Stream<Method> accessibleMethods(Class<?> klass) {
        return declaredMethodsInAncestors(klass).filter(method -> !Members.isPrivate(method));
    }

    public static Stream<Method> declaredMethodsInAncestors(Class<?> klass) {
        return Types.ancestors(klass)
                    .stream()
                    .flatMap(ancestor -> Stream.of(ancestor.getDeclaredMethods()))
                    .distinct();
    }

    public static <T> Stream<Invokable<T, Object>> declaredMethodsInAncestors(TypeToken<T> klass) {
        return declaredMethodsInAncestors(klass.getRawType()).map(klass::method);
    }

    /**
     * Return a collection of all methods in the given class that have the given annotation
     */
    public static <T extends Annotation> Collection<Method> annotatedMethods(Class<?> klass, final Class<T> annotation) {
        return Collections2.filter(Arrays.asList(klass.getMethods()),
                                   method -> method.getAnnotation(annotation) != null);
    }

    /**
     * Get the first annotation of the given type on the given method or any of its supermethods.
     * Supermethods are searched in the same order as {@link Types#ancestors(Class, boolean)}.
     */
    public static @Nullable <T extends Annotation> T inheritableAnnotation(Method method, Class<T> annotationType) {
        {
            final T annotation = method.getAnnotation(annotationType);
            if(annotation != null) return annotation;
        }

        if(!Members.isInheritable(method)) return null;

        return Types.findForAncestor(method.getDeclaringClass(), t -> {
            final Method parent = overrideIn(t, method);
            return parent == null ? null : parent.getAnnotation(annotationType);
        });
    }

    public static MethodType methodType(Method method) {
        return MethodType.methodType(method.getReturnType(), method.getParameterTypes());
    }

    public static MethodType methodType(TypeToken<?> returnType, Collection<Parameter> parameters) {
        return MethodType.methodType(returnType.getRawType(),
                                     parameters.stream()
                                               .map(p -> p.getType().getRawType())
                                               .collect(Collectors.toList()));
    }

    public static MethodType methodType(Invokable method) {
        return methodType(method.getReturnType(), method.getParameters());
    }

    private static @Nullable String invocationFailureReason(MethodType to, MethodType from) {
        if(!Types.isConvertibleForInvocation(to.returnType(), from.returnType())) {
            return "cannot convert return value from " + from.returnType().getName() + " to " + to.returnType().getName();
        }

        if(to.parameterCount() != from.parameterCount()) {
            return "required " + to.parameterCount() + " parameters, but " + from.parameterCount() + " provided";
        }

        for(int i = 0; i < to.parameterCount(); i++) {
            if(!Types.isConvertibleForInvocation(from.parameterType(i), to.parameterType(i))) {
                return "cannot convert parameter " + i + " from " + to.parameterType(i).getName() + " to " + from.parameterType(i).getName();
            }
        }

        return null;
    }

    private static @Nullable String invocationFailureReason(Invokable<?, ?> to, Invokable<?, ?> from) {
        final String reason = invocationFailureReason(methodType(to), methodType(from));
        if(reason != null) return reason;

        thrownLoop: for(TypeToken<? extends Throwable> thrown : from.getExceptionTypes()) {
            final Class<?> thrownRaw = thrown.getRawType();
            if(Error.class.isAssignableFrom(thrownRaw)) continue;
            if(RuntimeException.class.isAssignableFrom(thrownRaw)) continue ;
            for(TypeToken<? extends Throwable> caught : to.getExceptionTypes()) {
                if(caught.getRawType().isAssignableFrom(thrownRaw)) continue thrownLoop;
            }
            return "unhandled exception " + thrown.getRawType().getName();
        }

        return null;
    }

    public static @Nullable Method trySamMethod(Class<?> iface) {
        if(!iface.isInterface()) return null;

        final Method[] methods = iface.getMethods();
        if(methods.length == 1) {
            return methods[0];
        }

        Method sam = null;
        boolean first = true;
        for(Method method : methods) {
            if(Members.isStatic(method)) continue;

            if(first) {
                // If we've only seen one method, assume it's the SAM
                first = false;
                sam = method;
            } else {
                // If there are multiple methods, and we initially assumed that a default method was the
                // SAM, reverse that assumption.
                if(sam != null && sam.isDefault()) {
                    sam = null;
                }

                // If we find an abstract method, and we already have a SAM (which must also be abstract),
                // then the interface is non-functional. Otherwise, assume this method is the SAM and keep
                // going (to make sure there are no more abstract methods).
                if(!method.isDefault()) {
                    if(sam != null) {
                        return null;
                    }
                    sam = method;
                }
            }
        }

        return sam;
    }

    public static boolean isFunctionalInterface(Class<?> iface) {
        return trySamMethod(iface) != null;
    }

    public static Method samMethod(Class<?> iface) {
        final Method method = trySamMethod(iface);
        if(method == null) {
            throw new ClassFormException(iface, "not a functional interface");
        }
        return method;
    }

    public static @Nullable <T> Invokable<T, Object> trySamInvokable(TypeToken<T> iface) {
        final Method method = trySamMethod(iface.getRawType());
        return method == null ? null : iface.method(method);
    }

    public static <T> Invokable<T, Object> samInvokable(TypeToken<T> iface) {
        return iface.method(samMethod(iface.getRawType()));
    }

    //public static boolean implementsFunctionalInterface(Class<?> iface, Invokable method) {
    //    return isSignatureOverride(samMethod(iface), method);
    //}

    public static <T, U> T lambda(Class<T> samType, Method method, @Nullable U target) {
        return lambda(TypeToken.of(samType), method, target);
    }

    /**
     * Implement the given functional interface by delegating to the given
     * method and target object. The signature of the method is verified to
     * be compatible with the interface, using the same rules as lexical
     * method references.
     */
    public static <T, U> T lambda(TypeToken<T> samType, Method implMethod, @Nullable U target) {
        final boolean instance = !Members.isStatic(implMethod);
        if(instance) {
            Preconditions.checkNotNull(target);
        } else {
            checkArgument(target == null);
        }

        final MethodHandles.Lookup lookup;
        final MethodType callSiteType;
        final Invokable<U, Object> implInvokable;

        if(instance) {
            lookup = MethodHandleUtils.privateLookup(target.getClass());
            callSiteType = MethodType.methodType(samType.getRawType(), target.getClass());
            implInvokable = (Invokable<U, Object>) TypeToken.of(target.getClass()).method(implMethod);
        } else {
            lookup = MethodHandleUtils.privateLookup(implMethod.getDeclaringClass());
            callSiteType = MethodType.methodType(samType.getRawType());
            implInvokable = (Invokable<U, Object>) Invokable.from(implMethod);
        }

        final Method samMethod = samMethod(samType.getRawType());
        final MethodType samMethodType = methodType(samMethod);
        final Invokable<T, Object> samInvokable = samType.method(samMethod);
        final MethodType samInvokableType = methodType(samInvokable);

        final String error = invocationFailureReason(samInvokable, implInvokable);
        if(error != null) {
            throw new MethodFormException(implMethod, "could not be adapted to functional interface " + samType + ": " + error);
        }

        try {
            final MethodHandle factory = LambdaMetafactory.metafactory(
                lookup,
                samMethod.getName(),
                callSiteType,
                samMethodType,
                lookup.unreflect(implMethod),
                samInvokableType
            ).getTarget();

            return (T) (instance ? factory.invoke(target) : factory.invoke());

        } catch(Throwable e) {
            throw new MethodFormException(implMethod, "could not be adapted to functional interface " + samType, e);
        }
    }
}
