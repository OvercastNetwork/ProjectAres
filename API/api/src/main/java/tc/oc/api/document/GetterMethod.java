package tc.oc.api.document;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import javax.annotation.Nullable;

import tc.oc.api.docs.virtual.Document;
import tc.oc.commons.core.reflect.Methods;
import tc.oc.commons.core.util.ExceptionUtils;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Metadata for a getter method of a {@link Document} property.
 *
 * The wrapped method takes no parameters and returns the value of the property.
 */
public class GetterMethod<T> extends BaseAccessor<T> implements Getter<T> {

    private final Method method;

    public GetterMethod(DocumentRegistry registry, Method method) {
        super(registry);
        this.method = checkNotNull(method);
        method.setAccessible(true);
    }

    @Override
    public Method member() {
        return method;
    }

    @Override
    public Type type() {
        return method.getGenericReturnType();
    }

    @Override
    public Class<T> rawType() {
        return (Class<T>) method.getReturnType();
    }

    @Override
    protected @Nullable Accessor<?> getOverrideIn(DocumentMeta<?> ancestor) {
        return ancestor.getters().get(name());
    }

    @Override
    public boolean isImplemented(Class<?> type) {
        return Methods.respondsTo(type, method);
    }

    @Override
    public T get(Object obj) {
        try {
            return validate((T) member().invoke(obj));
        } catch(IllegalAccessException | InvocationTargetException e) {
            throw ExceptionUtils.propagate(e);
        }
    }
}
