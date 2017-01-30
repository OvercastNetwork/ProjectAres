package tc.oc.api.document;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;

import com.google.common.util.concurrent.UncheckedExecutionException;

/**
 * Property setter that wraps a field
 */
public class FieldSetter<T> extends FieldAccessor<T> implements Setter<T> {

    public FieldSetter(DocumentRegistry registry, Field field) {
        super(registry, field);
    }

    @Override
    protected @Nullable Accessor<?> getOverrideIn(DocumentMeta<?> ancestor) {
        return ancestor.setters().get(name());
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
            member().set(obj, validate(value));
        } catch(IllegalAccessException e) {
            throw new ExecutionException(e);
        }
    }
}
