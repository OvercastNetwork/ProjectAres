package tc.oc.commons.core.inject;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.inject.Provider;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.internal.Annotations;
import com.google.inject.internal.Errors;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.HasDependencies;
import com.google.inject.spi.InjectionPoint;
import com.google.inject.spi.ProviderWithDependencies;
import tc.oc.commons.core.ListUtils;
import tc.oc.commons.core.reflect.Members;
import tc.oc.commons.core.reflect.MethodHandleUtils;
import tc.oc.commons.core.reflect.MethodScanner;
import tc.oc.commons.core.reflect.Methods;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.commons.core.util.ThrowingRunnable;
import tc.oc.commons.core.util.ThrowingSupplier;

import static com.google.common.base.Preconditions.checkArgument;

public class InjectableMethod<T> implements HasDependencies, Module {

    private final Method method;
    private final @Nullable T result;
    private final MethodHandle handle;
    private final Key<T> providedKey;
    private final Class<? extends Annotation> scope;

    private final Set<Dependency<?>> dependencies;

    private List<Provider<?>> providers;

    private <D> InjectableMethod(@Nullable TypeLiteral<D> targetType, @Nullable D target, Method method, @Nullable T result) {
        final Errors errors = new Errors(method);

        if(Members.isStatic(method)) {
            checkArgument(target == null);
        } else {
            checkArgument(target != null);
        }

        targetType = targetType(targetType, target, method);

        checkArgument(method.getDeclaringClass().isAssignableFrom(targetType.getRawType()));

        this.method = method;
        this.dependencies = ImmutableSet.copyOf(InjectionPoint.forMethod(method, targetType).getDependencies());

        if(result != null) {
            this.result = result;
            this.providedKey = Keys.forInstance(result);
        } else {
            final TypeLiteral<T> returnType = (TypeLiteral<T>) targetType.getReturnType(method);
            if(!Void.class.equals(returnType.getRawType())) {
                final Annotation qualifier = Annotations.findBindingAnnotation(errors, method, method.getAnnotations());
                this.result = null;
                this.providedKey = Keys.get(returnType, qualifier);
            } else {
                this.result = (T) this;
                this.providedKey = Keys.forInstance(this.result);
            }
        }

        this.scope = Annotations.findScopeAnnotation(errors, method.getAnnotations());

        MethodHandle handle = MethodHandleUtils.privateUnreflect(method);
        if(target != null) {
            handle = handle.bindTo(target);
        }
        this.handle = handle;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{method=" + method + "}";
    }

    @Override
    public int hashCode() {
        return method.hashCode();
    }

    @Override
    public boolean equals(Object that) {
        return this == that || (
            that instanceof InjectableMethod &&
            method.equals(((InjectableMethod) that).method())
        );
    }

    public Method method() {
        return method;
    }

    public Key<T> key() {
        return providedKey;
    }

    public Class<? extends Annotation> scope() {
        return scope;
    }

    public boolean hasReturnValue() {
        return result == null;
    }

    @Override
    public Set<Dependency<?>> getDependencies() {
        return dependencies;
    }

    @Override
    public void configure(Binder binder) {
        binder = binder.withSource(method);
        providers = ListUtils.transformedCopyOf(dependencies, binder::getProvider);
    }

    public Module bindingModule() {
        return new KeyedManifest.Impl(method) {
            @Override
            public void configure() {
                final Binder binder = binder().withSource(method);
                if(!hasReturnValue()) {
                    binder.addError("Cannot bind this method as a provider because it does not return a value");
                    return;
                }

                install(InjectableMethod.this);

                final ScopedBindingBuilder builder = binder.bind(providedKey).toProvider(asProvider());
                if(scope != null) builder.in(scope);
            }
        };
    }

    private List<Provider<?>> providers() {
        if(providers == null) {
            throw new ProvisionException("Dependencies have not been injected (is the dependency module installed?)");
        }
        return providers;
    }

    public T invoke() throws Throwable {
        final T t = (T) handle.invokeWithArguments(Lists.transform(providers(), Provider::get));
        return result != null ? result : t;
    }

    public Provider<T> asProvider() {
        return new ProviderWithDependencies<T>() {
            @Override
            public T get() {
                return Injection.wrappingExceptions(asSupplier());
            }

            @Override
            public Set<Dependency<?>> getDependencies() {
                return dependencies;
            }
        };
    }

    public ThrowingSupplier<T, Throwable> asSupplier() {
        //if(!hasReturnValue()) {
        //    throw new ProvisionException("Cannot use this method as a Supplier because it has no return value (try calling asRunnable() instead)");
        //}
        return this::invoke;
    }

    public ThrowingRunnable<Throwable> asRunnable() {
        //if(hasReturnValue()) {
        //    throw new ProvisionException("Cannot use this method as a Runnable because it has a return value (try calling asSupplier() instead)");
        //}
        return this::invoke;
    }

    public static <D, T> InjectableMethod<T> forMethod(TypeLiteral<D> targetType, @Nullable D target, Method method, T result) {
        return new InjectableMethod<>(targetType, target, method, result);
    }

    public static <D> InjectableMethod<?> forMethod(TypeLiteral<D> targetType, @Nullable D target, Method method) {
        return new InjectableMethod<>(targetType, target, method, null);
    }

    public static <D> InjectableMethod<?> forDeclaredMethod(Class<D> targetType, String name, Class<?>... params) {
        return forDeclaredMethod(targetType, null, name, params);
    }

    public static <D> InjectableMethod<?> forDeclaredMethod(D target, String name, Class<?>... params) {
        return forDeclaredMethod((TypeLiteral<D>) null, target, name, params);
    }

    public static <D> InjectableMethod<?> forDeclaredMethod(@Nullable Class<D> targetType, @Nullable D target, String name, Class<?>... params) {
        return forDeclaredMethod(targetType == null ? null : TypeLiteral.get(targetType), target, name, params);
    }

    public static <D> InjectableMethod<?> forDeclaredMethod(@Nullable TypeLiteral<D> targetType, @Nullable D target, String name, Class<?>... params) {
        targetType = targetType(targetType, target, null);
        return forMethod(targetType, target, Methods.declaredMethod(targetType.getRawType(), name, params));
    }

    public static <D, A extends Annotation> List<InjectableMethod<?>> forAnnotatedMethods(@Nullable TypeLiteral<D> targetType, @Nullable D target, Class<A> annotationType) {
        return forInheritedMethods(targetType, target, method -> method.getAnnotation(annotationType) != null);
    }

    public static <D> List<InjectableMethod<?>> forInheritedMethods(@Nullable TypeLiteral<D> targetType, @Nullable D target, Predicate<? super Method> filter) {
        final TypeLiteral<D> finalTargetType = targetType(targetType, target, null);
        return new MethodScanner<>(finalTargetType, filter)
            .methods()
            .stream()
            .map(method -> (InjectableMethod<?>) forMethod(finalTargetType, target, method))
            .collect(Collectors.toImmutableList());
    }

    public static <D> List<InjectableMethod<?>> forDeclaredMethods(@Nullable TypeLiteral<D> targetType, @Nullable D target, Predicate<? super Method> filter) {
        final TypeLiteral<D> finalTargetType = targetType(targetType, target, null);
        return Stream
            .of(targetType.getRawType().getDeclaredMethods())
            .filter(filter)
            .map(method -> (InjectableMethod<?>) forMethod(finalTargetType, target, method))
            .collect(Collectors.toImmutableList());
    }

    private static <D> TypeLiteral<D> targetType(@Nullable Class<D> targetType, @Nullable D target, @Nullable Method method) {
        return targetType(targetType == null ? null : TypeLiteral.get(targetType), target, method);
    }

    private static <D> TypeLiteral<D> targetType(@Nullable TypeLiteral<D> targetType, @Nullable D target, @Nullable Method method) {
        if(targetType != null) return targetType;
        if(target != null) return TypeLiteral.get((Class<D>) target.getClass());
        return TypeLiteral.get((Class<D>) method.getDeclaringClass());
    }
}
