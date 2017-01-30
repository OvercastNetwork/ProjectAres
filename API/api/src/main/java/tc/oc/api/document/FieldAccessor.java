package tc.oc.api.document;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

public abstract class FieldAccessor<T> extends BaseAccessor<T> {

    private final Field field;

    public FieldAccessor(DocumentRegistry registry, Field field) {
        super(registry);
        this.field = field;
        field.setAccessible(true);
    }

    @Override
    public Type type() {
        return field.getGenericType();
    }

    @Override
    public Class<T> rawType() {
        return (Class<T>) field.getType();
    }

    @Override
    public Field member() {
        return field;
    }

    @Override
    public boolean isImplemented(Class<?> type) {
        return field.getDeclaringClass().isAssignableFrom(type);
    }
}
