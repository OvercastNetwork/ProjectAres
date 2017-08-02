package tc.oc.commons.core.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.inject.Provider;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.OutOfScopeException;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.internal.Annotations;
import com.google.inject.internal.Errors;
import com.google.inject.internal.ProviderMethodsModule;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.InjectionPoint;
import com.google.inject.spi.ModuleAnnotatedMethodScanner;
import tc.oc.commons.core.reflect.FieldDelegate;
import tc.oc.commons.core.reflect.Types;
import tc.oc.commons.core.util.Optionals;
import tc.oc.commons.core.util.ThrowingFunction;
import tc.oc.commons.core.util.ThrowingRunnable;
import tc.oc.commons.core.util.ThrowingSupplier;

public class Injection {
    /**
     * Is the given type a {@link Provider} of some other type? Works for
     */
    public static boolean isImplicitProvider(TypeLiteral<?> type) {
        return type.getType() instanceof ParameterizedType &&
               Provider.class.isAssignableFrom(type.getRawType());
    }

    /**
     * Return the type provided by the given {@link Provider}
     */
    public static <T> TypeLiteral<T> providedType(TypeLiteral<? extends Provider<T>> providerType) {
        return (TypeLiteral<T>) TypeLiteral.get(((ParameterizedType) providerType.getType()).getActualTypeArguments()[0]);
    }

    /**
     * Return the type that is depended on by injecting the given type.
     * These types are the same, unless the injected type is a {@link Provider},
     * in which case the dependency is on the provided type.
     */
    public static TypeLiteral<?> dependencyType(TypeLiteral<?> injectedType) {
        if(isImplicitProvider(injectedType)) {
            return providedType((TypeLiteral<? extends Provider<Object>>) injectedType);
        } else {
            return injectedType;
        }
    }

    public static Key<?> dependencyKey(Key<?> key) {
        if(isImplicitProvider(key.getTypeLiteral())) {
            return Key.get(providedType((TypeLiteral<? extends Provider<Object>>) key.getTypeLiteral()));
        } else {
            return key;
        }
    }

    /**
     * Return all direct dependencies injected into the given type
     */
    public static Stream<Dependency<?>> dependencies(Class<?> type) {
        return Stream.concat(
            Stream.of(InjectionPoint.forConstructorOf(type)),
            InjectionPoint.forInstanceMethodsAndFields(type).stream()
        ).flatMap(ip -> ip.getDependencies().stream());
    }

    public static Optional<Class<? extends Annotation>> scopeAnnotation(Class<?> type) {
        return Optional.ofNullable(Annotations.findScopeAnnotation(new Errors(), type));
    }

    public static RuntimeException wrapException(Throwable e) {
        if(e instanceof RuntimeException) {
            throw (RuntimeException) e;
        } else if(e instanceof Error) {
            throw (Error) e;
        } else {
            throw new ProvisionException("Provisioning error", e);
        }
    }

    public static <E extends Throwable> E unwrapException(Class<E> type, ProvisionException e) throws E {
        if(e.getCause() instanceof RuntimeException) {
            throw (RuntimeException) e.getCause();
        } else if(type.isInstance(e.getCause())) {
            throw (E) e.getCause();
        } else {
            throw e;
        }
    }

    public static <T, E extends Throwable> T unwrappingExceptions(Class<E> exception, Provider<T> block) throws E {
        try {
            return block.get();
        } catch(ProvisionException e) {
            throw unwrapException(exception, e);
        }
    }

    public static <E extends Throwable> void unwrappingExceptions(Class<E> exception, ThrowingRunnable<E> block) throws E {
        try {
            block.runThrows();
        } catch(ProvisionException e) {
            throw unwrapException(exception, e);
        }
    }

    public static <T, E extends Throwable> T unwrappingExceptions(Class<E> exception, ThrowingSupplier<T, E> block) throws E {
        try {
            return block.getThrows();
        } catch(ProvisionException e) {
            throw unwrapException(exception, e);
        }
    }

    public static <E extends Throwable> ThrowingRunnable<E> unwrapExceptions(Class<E> exception, ThrowingRunnable<E> block) {
        return () -> unwrappingExceptions(exception, block);
    }

    public static <T, E extends Throwable> ThrowingSupplier<T, E> unwrapExceptions(Class<E> exception, ThrowingSupplier<T, E> block) {
        return () -> unwrappingExceptions(exception, block);
    }

    public static ThrowingRunnable<Throwable> unwrapExceptions(ThrowingRunnable<Throwable> block) {
        return () -> unwrappingExceptions(Throwable.class, block);
    }

    public static <T> ThrowingSupplier<T, Throwable> unwrapExceptions(ThrowingSupplier<T, Throwable> block) {
        return () -> unwrappingExceptions(Throwable.class, block);
    }

    public static <T> T wrappingExceptions(ThrowingSupplier<T, Throwable> block) {
        try {
            return block.getThrows();
        } catch(Throwable e) {
            throw wrapException(e);
        }
    }

    public static void wrappingExceptions(ThrowingRunnable<Throwable> block) {
        try {
            block.runThrows();
        } catch(Throwable e) {
            throw wrapException(e);
        }
    }

    public static <T, R> Function<T, R> wrappingExceptions(ThrowingFunction<T, R, Throwable> block) {
        return x -> {
            try {
                return block.apply(x);
            } catch(Throwable e) {
                throw wrapException(e);
            }
        };
    }

    public static <T> Optional<T> getIfInScope(Provider<T> provider) {
        try {
            return Optional.of(provider.get());
        } catch(ProvisionException e) {
            if(e.getCause() instanceof OutOfScopeException) {
                // This is what actually happens with Guice providers
                return Optional.empty();
            }
            throw e;
        } catch(OutOfScopeException e) {
            // Handle this just in case
            return Optional.empty();
        }
    }

    public static Stream<Binding<?>> bindings(Injector injector) {
        return injector == null ? Stream.empty()
                                : Stream.concat(injector.getBindings().values().stream(),
                                                bindings(injector.getParent()));
    }

    public static <T> Stream<Binding<? extends T>> bindingsAssignableTo(Injector injector, TypeLiteral<T> baseType) {
        return (Stream) bindings(injector).filter(binding -> Types.isAssignable(baseType, binding.getKey().getTypeLiteral()));
    }

    public static <T> Stream<Key<? extends T>> keysAssignableTo(Injector injector, TypeLiteral<T> baseType) {
        return bindingsAssignableTo(injector, baseType).map(Binding::getKey);
    }

    public static <T> Stream<TypeLiteral<? extends T>> keyTypesAssignableTo(Injector injector, TypeLiteral<T> baseType) {
        return keysAssignableTo(injector, baseType).map(Key::getTypeLiteral);
    }

    public static void forEachBinding(Injector injector, Consumer<? super Binding<?>> consumer) {
        while(injector != null) {
            injector.getBindings().forEach((key, binding) -> consumer.accept(binding));
            injector = injector.getParent();
        }
    }

    public static <T> Map<Key<? extends T>, Binding<? extends T>> bindingsAssignableTo(Injector injector, Class<T> type) {
        return bindingsAssignableTo(injector, TypeToken.of(type));
    }

    public static <T> Map<Key<? extends T>, Binding<? extends T>> bindingsAssignableTo(Injector injector, TypeToken<T> type) {
        final ImmutableMap.Builder<Key<? extends T>, Binding<? extends T>> builder = ImmutableMap.builder();
        forEachBinding(injector, binding -> {
            if(type.isAssignableFrom(binding.getKey().getTypeLiteral().getType())) {
                builder.put((Key<? extends T>) binding.getKey(), (Binding<? extends T>) binding);
            }
        });
        return builder.build();
    }

    /**
     * HACK!
     *
     * Scan the given module for methods annotated with {@link ProvidesGeneric} and generate provider methods
     * from them. This works exactly like Guice's built-in provider methods, except that the owner type can
     * be specified explicitly, allowing for generic provider methods.
     *
     * HACK: Reflectively change the module type in ProviderMethodsModule, before it scans for provider methods.
     * This allows the actual type of a generic module to be provided at runtime.
     */
    public static <T extends Module> Module providerMethodsModule(T hostModule, TypeLiteral<T> type) {
        final ProviderMethodsModule module = (ProviderMethodsModule) ProviderMethodsModule.forModule(hostModule, SCANNER);
        ProviderMethodsModule_typeLiteral.set(module, type);
        return module;
    }

    private static final FieldDelegate.Instance<ProviderMethodsModule, TypeLiteral<?>> ProviderMethodsModule_typeLiteral =
        FieldDelegate.Instance.forField(ProviderMethodsModule.class, new TypeToken<TypeLiteral<?>>(){}, "typeLiteral");

    private static final ModuleAnnotatedMethodScanner SCANNER = new ModuleAnnotatedMethodScanner() {
        @Override public Set<? extends Class<? extends Annotation>> annotationClasses() { return ImmutableSet.of(ProvidesGeneric.class); }
        @Override public <T> Key<T> prepareMethod(Binder binder, Annotation annotation, Key<T> key, InjectionPoint injectionPoint) { return key; }
    };

    public static <T> Constructor<T> injectableConstructor(TypeLiteral<T> type) {
        return (Constructor<T>) InjectionPoint.forConstructorOf(type).getMember();
    }

    public static <T> Constructor<T> injectableConstructor(Class<T> type) {
        return injectableConstructor(TypeLiteral.get(type));
    }
}
