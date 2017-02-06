package tc.oc.commons.core.inject;

import java.lang.annotation.Annotation;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import javax.inject.Inject;

import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Scope;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.internal.Scoping;
import com.google.inject.matcher.Matcher;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.multibindings.OptionalBinder;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
import com.google.inject.spi.ProvisionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import tc.oc.commons.core.reflect.ResolvableType;
import tc.oc.commons.core.reflect.TypeArgument;
import tc.oc.commons.core.reflect.TypeParameter;
import tc.oc.commons.core.reflect.TypeResolver;
import tc.oc.commons.core.reflect.Types;
import tc.oc.commons.core.util.Functions;
import tc.oc.commons.core.util.ProxyUtils;
import tc.oc.inject.ForwardingBinder;

public interface Binders extends ForwardingBinder {

    static Binders wrap(Binder binder) {
        if(binder instanceof Binders) {
            return (Binders) binder;
        }
        final Binder skipped = binder.skipSources(Binders.class, ForwardingBinder.class);
        return () -> skipped;
    }

    /**
     * Adapt a {@link Consumer} to be a {@link ProvisionListener} (which is not lambda compatible)
     */
    static ProvisionListener provisionListener(Consumer<ProvisionListener.ProvisionInvocation<?>> consumer) {
        return consumer::accept;
    }

    static Predicate<Binding<?>> bindingsForSubtypesOf(TypeLiteral<?> type) {
        return binding -> Types.isAssignable(type, binding.getKey().getTypeLiteral());
    }

    static Predicate<Binding<?>> bindingsForSubtypesOf(Class<?> type) {
        return bindingsForSubtypesOf(TypeLiteral.get(type));
    }

    default <T> void bindSubtypesOfListener(Class<T> type, SubtypeListener<T> listener) {
        bindSubtypesOfListener(TypeLiteral.get(type), listener);
    }

    default <T> void bindSubtypesOfListener(TypeLiteral<T> type, SubtypeListener<T> listener) {
        bindListener((Matcher<? super TypeLiteral<?>>) Matchers.subtypesOf(type), new TypeListener() {
            @Override
            public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
                listener.hear((TypeLiteral<T>) type, (TypeEncounter<T>) encounter);
            }
        });
    }

    /**
     * Adapt the given matcher and listener and pass them to {@link Binder#bindListener(Matcher, ProvisionListener...)}
     */
    default void bindProvisionListener(Predicate<? super Binding<?>> matcher, Consumer<ProvisionListener.ProvisionInvocation<?>> listener) {
        bindListener(Matchers.predicate(matcher), provisionListener(listener));
    }

    default <T> void bindProvisionSubtypesOfListener(TypeLiteral<T> type, Consumer<ProvisionListener.ProvisionInvocation<T>> listener) {
        bindProvisionListener(bindingsForSubtypesOf(type), (Consumer) listener);
    }

    default <T> void bindProvisionSubtypesOfListener(Class<T> type, Consumer<ProvisionListener.ProvisionInvocation<T>> listener) {
        bindProvisionSubtypesOfListener(TypeLiteral.get(type), listener);
    }

    default void bindToOwnClass(Object obj) {
        bind((Class) obj.getClass()).toInstance(obj);
    }

    default <T> Multibinder<T> inSet(Key<T> key) {
        return Multibinder.newSetBinder(forwardedBinder(), key);
    }
    default <T> Multibinder<T> inSet(TypeLiteral<T> type) { return inSet(Key.get(type)); }
    default <T> Multibinder<T> inSet(Class<T> type) { return inSet(Key.get(type)); }

    default <T> OptionalBinder<T> forOptional(Key<T> key) {
        return OptionalBinder.newOptionalBinder(forwardedBinder(), key);
    }
    default <T> OptionalBinder<T> forOptional(TypeLiteral<T> type) { return forOptional(Key.get(type)); }
    default <T> OptionalBinder<T> forOptional(Class<T> type) { return forOptional(Key.get(type)); }

    default <T> void installFactory(Key<T> key) {
        install(new FactoryModuleBuilder().build(key));
    }
    default <T> void installFactory(TypeLiteral<T> type) { installFactory(Key.get(type)); }
    default <T> void installFactory(Class<T> type) { installFactory(Key.get(type)); }

    default <T> void installInnerClassFactory(Key<T> key) {
        install(InnerFactoryManifest.forInnerClass(key));
    }
    default <T> void installInnerClassFactory(TypeLiteral<T> type) { installInnerClassFactory(Key.get(type)); }
    default <T> void installInnerClassFactory(Class<T> type) { installInnerClassFactory(Key.get(type)); }

    default <T> T getProxy(Key<T> key) {
        return ProxyUtils.newProviderProxy(key.getTypeLiteral(), getProvider(key));
    }
    default <T> T getProxy(TypeLiteral<T> type) { return getProxy(Key.get(type)); }
    default <T> T getProxy(Class<T> type) { return getProxy(Key.get(type)); }

    default <T> void bindProxy(TypeLiteral<T> type) {
        install(new ProxiedManifest<>(type));
    }
    default <T> void bindProxy(Class<T> type) { bindProxy(TypeLiteral.get(type)); }

    default <T> void linkOptional(Key<T> key) {
        final TypeResolver resolver = new TypeResolver().where(new TypeParameter<T>(){}, key.getTypeLiteral());
        bind(Keys.optional(key))
            .toProvider(key.ofType(resolver.resolve(new TypeLiteral<OptionalProvider<T>>(){})));
    }

    default <T> void linkOptional(TypeLiteral<T> type) { linkOptional(Key.get(type)); }
    default <T> void linkOptional(Class<T> type) { linkOptional(Key.get(type)); }

    default void installIn(Scoping scoping, Module... modules) {
        final Scoper scoper = new Scoper(this, scoping);
        for(Element element : Elements.getElements(modules)) {
            if(element instanceof Binding) {
                ((Binding) element).acceptTargetVisitor(scoper);
            } else {
                element.applyTo(this);
            }
        }
    }

    default void installIn(Scope scope, Module... modules) { installIn(Scoping.forInstance(scope), modules); }
    default void installIn(Class<? extends Annotation> scope, Module... modules) { installIn(Scoping.forAnnotation(scope), modules); }

    default <T> T memberInjected(TypeLiteral<T> type, T instance) {
        requestInjection(type, instance);
        return instance;
    }

    default <T> T memberInjected(T instance) {
        requestInjection(instance);
        return instance;
    }

    default <T> UnaryOperator<T> membersInjector(TypeLiteral<T> type) {
        return Functions.tapUnlessNull(getMembersInjector(type)::injectMembers);
    }

    default <T> UnaryOperator<T> membersInjector(Class<T> type) {
        return membersInjector(TypeLiteral.get(type));
    }

    class EagerProvisioner<T> {
        @Inject EagerProvisioner(T t) {}
    }

    default <T> void provisionEagerly(Key<T> key) {
        bind(key.ofType(new ResolvableType<EagerProvisioner<T>>(){}.with(new TypeArgument<T>(key.getTypeLiteral()){})))
            .asEagerSingleton();
    }

    default <T> void provisionEagerly(TypeLiteral<T> type) { provisionEagerly(Key.get(type)); }
    default <T> void provisionEagerly(Class<T> type) { provisionEagerly(Key.get(type)); }

    @Override
    default Binders withSource(Object source) {
        return wrap(ForwardingBinder.super.withSource(source));
    }

    @Override
    default Binders skipSources(Class... classesToSkip) {
        return wrap(ForwardingBinder.super.skipSources(classesToSkip));
    }

    @Override
    default PrivateBinders newPrivateBinder() {
        return PrivateBinders.wrap(ForwardingBinder.super.newPrivateBinder());
    }
}

