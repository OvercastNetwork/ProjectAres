package tc.oc.api.document;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;

import com.google.common.util.concurrent.UncheckedExecutionException;
import tc.oc.api.docs.virtual.Document;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Metadata for a setter method of a {@link Document} property.
 *
 * The wrapped method takes the new value as its single parameter.
 */
public class SetterMethod<T> extends BaseAccessor<T> implements Setter<T> {

    private final Method method;

    public SetterMethod(DocumentRegistry registry, Method method) {
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
        return method.getGenericParameterTypes()[0];
    }

    @Override
    public Class<T> rawType() {
        return (Class<T>) method.getParameterTypes()[0];
    }

    @Override
    protected @Nullable Accessor<?> getOverrideIn(DocumentMeta<?> ancestor) {
        return ancestor.setters().get(name());
    }

    @Override
    public boolean isImplemented(Class<?> type) {
        try {
            return !Modifier.isAbstract(type.getMethod(method.getName(), method.getParameterTypes()[0]).getModifiers());
        } catch(NoSuchMethodException e) {
            return false;
        }
    }

    @Override
    public void setUnchecked(Object obj, T value) {
        try {
            set(obj, value);
        } catch(ExecutionException e) {
            throw new UncheckedExecutionException(e.getCause());
        }
    }

    @Override
    public void set(Object obj, T value) throws ExecutionException {
        try {
            method.invoke(obj, validate(value));
        } catch(IllegalAccessException | InvocationTargetException e) {
            throw new ExecutionException(e);
        }
    }
}
