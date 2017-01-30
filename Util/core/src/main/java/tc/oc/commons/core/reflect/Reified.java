package tc.oc.commons.core.reflect;

import com.google.common.reflect.TypeToken;
import com.google.inject.TypeLiteral;

/**
 * A generic type that knows its type parameter at runtime
 *
 * How it knows this is unknown
 */
public interface Reified<T> {

    default Class<T> paramClass() {
        return (Class<T>) paramToken().getRawType();
    }

    TypeToken<T> paramToken();

    default TypeLiteral<T> paramLiteral() {
        return Types.toLiteral(paramToken());
    }
}
