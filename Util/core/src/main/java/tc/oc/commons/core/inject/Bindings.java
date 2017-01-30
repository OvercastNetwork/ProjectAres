package tc.oc.commons.core.inject;

import java.util.Optional;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.ConstructorBinding;
import com.google.inject.spi.ExposedBinding;
import com.google.inject.spi.InstanceBinding;
import com.google.inject.spi.LinkedKeyBinding;
import com.google.inject.spi.UntargettedBinding;

public class Bindings {

    public static <T> Optional<TypeLiteral<? extends T>> targetType(Injector injector, Key<T> key) {
        return targetType(injector, injector.getBinding(key));
    }

    public static <T> Optional<TypeLiteral<? extends T>> targetType(Injector injector, Binding<T> binding) {
        if(binding instanceof UntargettedBinding) {
            return Optional.of(binding.getKey().getTypeLiteral());
        } else if(binding instanceof ConstructorBinding) {
            return Optional.of((TypeLiteral<? extends T>) ((ConstructorBinding) binding).getConstructor().getDeclaringType());
        } else if(binding instanceof InstanceBinding) {
            return Optional.of(TypeLiteral.get((Class<T>) ((InstanceBinding) binding).getInstance().getClass()));
        } else if(binding instanceof LinkedKeyBinding) {
            return targetType(injector, injector.getBinding(((LinkedKeyBinding) binding).getLinkedKey()));
        } else if(binding instanceof ExposedBinding) {
            return targetType(((ExposedBinding) binding).getPrivateElements().getInjector(), binding.getKey());
        }
        return Optional.empty();
    }
}
