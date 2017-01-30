package tc.oc.commons.core.inject;

import java.lang.reflect.Constructor;

import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.PrivateBinder;
import com.google.inject.Scope;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.internal.Scoping;
import com.google.inject.spi.BindingScopingVisitor;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.ConstructorBinding;
import com.google.inject.spi.ConvertedConstantBinding;
import com.google.inject.spi.Element;
import com.google.inject.spi.ExposedBinding;
import com.google.inject.spi.InjectionPoint;
import com.google.inject.spi.InstanceBinding;
import com.google.inject.spi.LinkedKeyBinding;
import com.google.inject.spi.ProviderBinding;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.ProviderKeyBinding;
import com.google.inject.spi.UntargettedBinding;

public class Scoper<T> implements BindingTargetVisitor<T, Void> {

    private final Binder binder;
    private final Scoping scoping;

    public Scoper(Binder binder, Scoping scoping) {
        this.binder = binder;
        this.scoping = scoping;
    }

    private LinkedBindingBuilder rebind(Binding binding) {
        return binder.bind(binding.getKey());
    }

    private void scope(Binding binding, ScopedBindingBuilder builder) {
        binding.acceptScopingVisitor(new BindingScopingVisitor<Void>() {
            @Override
            public Void visitEagerSingleton() {
                builder.asEagerSingleton();
                return null;
            }

            @Override
            public Void visitScope(Scope scope) {
                builder.in(scope);
                return null;
            }

            @Override
            public Void visitScopeAnnotation(Class scopeAnnotation) {
                builder.in(scopeAnnotation);
                return null;
            }

            @Override
            public Void visitNoScoping() {
                scoping.applyTo(builder);
                return null;
            }
        });
    }

    @Override
    public Void visit(ProviderInstanceBinding<? extends T> binding) {
        scope(binding, rebind(binding).toProvider(binding.getUserSuppliedProvider()));
        return null;
    }

    @Override
    public Void visit(ProviderKeyBinding<? extends T> binding) {
        scope(binding, rebind(binding).toProvider(binding.getProviderKey()));
        return null;
    }

    @Override
    public Void visit(LinkedKeyBinding<? extends T> binding) {
        scope(binding, rebind(binding).to(binding.getLinkedKey()));
        return null;
    }

    @Override
    public Void visit(UntargettedBinding<? extends T> binding) {
        scope(binding, rebind(binding));
        return null;
    }

    @Override
    public Void visit(ConstructorBinding<? extends T> binding) {
        final InjectionPoint point = binding.getConstructor();
        scope(binding, rebind(binding).toConstructor((Constructor) point.getMember(), point.getDeclaringType()));
        return null;
    }

    @Override
    public Void visit(ProviderBinding<? extends T> binding) {
        // These are only created internally, not sure why we would ever see it
        scope(binding, rebind(binding).toProvider(binding.getProvider()));
        return null;
    }

    @Override
    public Void visit(InstanceBinding<? extends T> binding) {
        // Cannot be scoped
        binding.applyTo(binder);
        return null;
    }

    @Override
    public Void visit(ConvertedConstantBinding<? extends T> binding) {
        // Cannot be scoped
        binding.applyTo(binder);
        return null;
    }

    @Override
    public Void visit(ExposedBinding<? extends T> binding) {
        final PrivateBinder privateBinder = this.binder.newPrivateBinder();
        final Scoper scoper = new Scoper(privateBinder, scoping);
        for(Element element : binding.getPrivateElements().getElements()) {
            if(element instanceof Binding) {
                ((Binding) element).acceptTargetVisitor(scoper);
            } else {
                element.applyTo(privateBinder);
            }
        }
        for(Key key : binding.getPrivateElements().getExposedKeys()) {
            privateBinder.expose(key);
        }
        return null;
    }
}
