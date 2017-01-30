package tc.oc.commons.core.inject;

import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.reflect.TypeParameter;
import com.google.inject.Key;
import tc.oc.commons.core.reflect.ResolvableType;

public class OptionalProvider<T> implements Provider<Optional<T>> {

    public static <T> Key<OptionalProvider<T>> key(Key<T> key) {
        return Key.get(
            new ResolvableType<OptionalProvider<T>>(){}
                .where(new TypeParameter<T>(){}, key.getTypeLiteral())
        );
    }

    final Provider<T> provider;

    @Inject
    OptionalProvider(Provider<T> provider) {
        this.provider = provider;
    }

    @Override
    public Optional<T> get() {
        return Optional.of(provider.get());
    }
}
