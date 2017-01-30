package tc.oc.pgm.module;

import java.util.Optional;
import javax.annotation.Nullable;

import com.google.common.reflect.TypeParameter;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import tc.oc.commons.core.reflect.ResolvableType;

/**
 * Contains the result of provisioning a module, and the module instance
 * itself, if it was provided.
 *
 * This exists in order to distinguish between absent modules and modules
 * that failed due to a user error. Knowing the difference allows us to
 * avoid chain reactions that can spew tons of superfluous errors.
 *
 * @see UpstreamProvisionFailure
 */
public class ProvisionWrapper<T> {

    static <T> Key<ProvisionWrapper<T>> keyOf(TypeLiteral<T> type) {
        return Key.get(new ResolvableType<ProvisionWrapper<T>>(){}.where(new TypeParameter<T>(){}, type));
    }

    final TypeLiteral<T> type;
    final ProvisionResult result;

    final @Nullable T instance;

    public ProvisionWrapper(TypeLiteral<T> type, ProvisionResult result) {
        this(type, result, null);
    }

    public ProvisionWrapper(TypeLiteral<T> type, ProvisionResult result, @Nullable T instance) {
        this.type = type;
        this.result = result;
        this.instance = instance;
    }

    public T require(@Nullable TypeLiteral<?> dependee) {
        switch(result) {
            case PRESENT: return instance;
            case FAILED: throw new UpstreamProvisionFailure();
        }

        // Don't throw a ProvisionException because it doesn't give you a stack trace
        if(dependee == null) {
            throw new IllegalStateException("Missing required module " + type);
        } else {
            throw new IllegalStateException("Missing module " + type + " which is required by " + dependee);
        }
    }

    public Optional<T> optional() {
        switch(result) {
            case PRESENT: return Optional.of(instance);
            case FAILED: throw new UpstreamProvisionFailure();
        }
        return Optional.empty();
    }
}
