package tc.oc.commons.core.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.internal.Annotations;
import com.google.inject.internal.Scoping;

/**
 * Base implementation for the Guice DSL builder class. Possibly useful for custom binders.
 */
public abstract class AbstractBindingBuilder<T> implements AnnotatedBindingBuilder<T> {

    protected abstract void applyScoping(Scoping scoping);

    @Override
    public LinkedBindingBuilder<T> annotatedWith(Class<? extends Annotation> annotationType) {
        return annotatedWith(Annotations.generateAnnotation(annotationType));
    }

    @Override
    public ScopedBindingBuilder to(Class<? extends T> implementation) {
        return to(Key.get(implementation));
    }

    @Override
    public ScopedBindingBuilder to(TypeLiteral<? extends T> implementation) {
        return to(Key.get(implementation));
    }

    @Override
    public ScopedBindingBuilder toProvider(Provider<? extends T> provider) {
        return toProvider((javax.inject.Provider<? extends T>) provider);
    }

    @Override
    public ScopedBindingBuilder toProvider(Class<? extends javax.inject.Provider<? extends T>> providerType) {
        return toProvider(Key.get(providerType));
    }

    @Override
    public ScopedBindingBuilder toProvider(TypeLiteral<? extends javax.inject.Provider<? extends T>> providerType) {
        return toProvider(Key.get(providerType));
    }

    @Override
    public <S extends T> ScopedBindingBuilder toConstructor(Constructor<S> constructor) {
        return toConstructor(constructor, TypeLiteral.get(constructor.getDeclaringClass()));
    }

    @Override
    public void in(Class<? extends Annotation> scopeAnnotation) {
        applyScoping(Scoping.forAnnotation(scopeAnnotation));
    }

    @Override
    public void in(Scope scope) {
        applyScoping(Scoping.forInstance(scope));
    }

    @Override
    public void asEagerSingleton() {
        applyScoping(Scoping.EAGER_SINGLETON);
    }
}
