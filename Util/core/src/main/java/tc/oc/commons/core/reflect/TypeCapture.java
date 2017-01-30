package tc.oc.commons.core.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static com.google.common.base.Preconditions.checkArgument;

public class TypeCapture<T> {

    protected Type capture() {
        Type superclass = getClass().getGenericSuperclass();
        checkArgument(superclass instanceof ParameterizedType,
                      "%s isn't parameterized", superclass);

        return ((ParameterizedType) superclass).getActualTypeArguments()[0];
    }
}
