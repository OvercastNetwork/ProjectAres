package tc.oc.api.document;

import java.lang.reflect.Field;
import javax.annotation.Nullable;

import tc.oc.commons.core.util.ExceptionUtils;

/**
 * Property getter that wraps a field
 */
public class FieldGetter<T> extends FieldAccessor<T> implements Getter<T> {

    public FieldGetter(DocumentRegistry registry, Field field) {
        super(registry, field);
    }

    @Override
    protected @Nullable Accessor<?> getOverrideIn(DocumentMeta<?> ancestor) {
        return ancestor.getters().get(name());
    }

    @Override
    public T get(Object obj) {
        try {
            return validate((T) member().get(obj));
        } catch(IllegalAccessException e) {
            throw ExceptionUtils.propagate(e);
        }
    }
}
