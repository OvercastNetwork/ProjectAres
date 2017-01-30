package tc.oc.commons.core.inject;

import java.util.Optional;

import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.ConstructorBinding;
import com.google.inject.spi.ConvertedConstantBinding;
import com.google.inject.spi.ExposedBinding;
import com.google.inject.spi.InstanceBinding;
import com.google.inject.spi.LinkedKeyBinding;
import com.google.inject.spi.ProviderBinding;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.ProviderKeyBinding;
import com.google.inject.spi.UntargettedBinding;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

/**
 * Try to determine the actual type that will be provisioned for a binding.
 * This should succeed for everything except provider bindings.
 */
public class BindingTargetTypeResolver implements BindingTargetVisitor<Object, Optional<TypeLiteral<?>>> {

    private final Injector injector;

    public BindingTargetTypeResolver(Injector injector) {
        this.injector = injector;
    }

    @Override
    public Optional<TypeLiteral<?>> visit(InstanceBinding<?> binding) {
        // Return the provisioned object's type
        return of(TypeLiteral.get(binding.getInstance().getClass()));
    }

    @Override
    public Optional<TypeLiteral<?>> visit(ConvertedConstantBinding<?> binding) {
        // Return the provisioned object's type
        return of(TypeLiteral.get(binding.getValue().getClass()));
    }

    @Override
    public Optional<TypeLiteral<?>> visit(UntargettedBinding<?> binding) {
        // Return the key type
        return of(binding.getKey().getTypeLiteral());
    }

    @Override
    public Optional<TypeLiteral<?>> visit(ConstructorBinding<?> binding) {
        // Return the owning type of the constructor
        return of(binding.getConstructor().getDeclaringType());
    }

    @Override
    public Optional<TypeLiteral<?>> visit(LinkedKeyBinding<?> binding) {
        // Delegate to the binding for the target type
        return injector.getBinding(binding.getLinkedKey()).acceptTargetVisitor(this);
    }

    @Override
    public Optional<TypeLiteral<?>> visit(ProviderBinding<?> binding) {
        // Delegate to the binding for the provided type
        return injector.getBinding(binding.getProvidedKey()).acceptTargetVisitor(this);
    }

    @Override
    public Optional<TypeLiteral<?>> visit(ExposedBinding<?> binding) {
        // Lookup the exposed key in the private environment.
        // Since this visitor can only be used on an injector binding,
        // the private child injector should always be present too.
        return ofNullable(binding.getPrivateElements().getInjector())
            .flatMap(child -> child.getBinding(binding.getKey())
                                   .acceptTargetVisitor(new BindingTargetTypeResolver(child)));
    }

    @Override
    public Optional<TypeLiteral<?>> visit(ProviderInstanceBinding<?> binding) {
        // We don't know what the provider will return
        return empty();
    }

    @Override
    public Optional<TypeLiteral<?>> visit(ProviderKeyBinding<?> binding) {
        // We don't know what the provider will return
        return empty();
    }
}
