package tc.oc.commons.core.reflect;

import com.google.common.reflect.TypeToken;

/**
 * A generic type that *might* be able to reolve its type parameter
 * at runtime using the supertypes of the instance. This will only
 * work if the instance's class fully specifies the type in its ancestry.
 *
 * If this is not always the case, then subtypes should override {@link #paramToken()}
 * to provide the type through some other means in those cases.
 */
public interface AutoReified<T> extends Reified<T> {
    @Override
    default TypeToken<T> paramToken() {
        return (TypeToken<T>) AutoReifiedSupport.T_CACHE.resolve(getClass());
    }
}

class AutoReifiedSupport {
    static final TypeParameterCache<AutoReified, Object> T_CACHE = new TypeParameterCache<>(AutoReified.class, "T");
}