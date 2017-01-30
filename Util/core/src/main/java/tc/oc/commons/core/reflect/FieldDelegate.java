package tc.oc.commons.core.reflect;

import java.lang.reflect.Field;

import com.google.common.reflect.TypeToken;

public abstract class FieldDelegate {

    private final Field field;

    public FieldDelegate(Field field) {
        this.field = field;
        field.setAccessible(true);
    }

    private Object get(Object obj) {
        try {
            return field.get(obj);
        } catch(IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }

    private void set(Object obj, Object value) {
        try {
            field.set(obj, value);
        } catch(IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }

    private static Field findField(Class<?> owner, TypeToken<?> type, String name) {
        try {
            final Field field = owner.getDeclaredField(name);
            final TypeToken<?> actualType = TypeToken.of(field.getGenericType());
            if(!type.equals(actualType)) {
                throw new NoSuchFieldError(
                    "Expected field " + Members.qualifiedName(field) +
                    " to have exact type " + type +
                    " but the actual type is " + actualType
                );
            }
            return field;
        } catch(NoSuchFieldException e) {
            throw new NoSuchFieldError(e.getMessage());
        }
    }

    public static class Static<T> extends FieldDelegate {

        public static <T> Static<T> forField(Class<?> owner, Class<T> type, String name) {
            return forField(owner, TypeToken.of(type), name);
        }

        public static <T> Static<T> forField(Class<?> owner, TypeToken<T> type, String name) {
            final Field field = findField(owner, type, name);
            if(!Members.isStatic(field)) {
                throw new NoSuchFieldError("Field " + Members.qualifiedName(field) + " is not static");
            }
            return new Static<>(field);
        }

        private Static(Field field) {
            super(field);
        }

        public T get() {
            return (T) super.get(null);
        }

        public void set(T value) {
            super.set(null, value);
        }
    }

    public static class Instance<O, T> extends FieldDelegate {

        public static <O, T> Instance<O, T> forField(Class<O> owner, Class<T> type, String name) {
            return forField(owner, TypeToken.of(type), name);
        }

        public static <O, T> Instance<O, T> forField(Class<O> owner, TypeToken<T> type, String name) {
            final Field field = findField(owner, type, name);
            if(Members.isStatic(field)) {
                throw new NoSuchFieldError("Field " + Members.qualifiedName(field) + " is static");
            }
            return new Instance<>(field);
        }

        private Instance(Field field) {
            super(field);
        }

        public T get(O instance) {
            return (T) super.get(instance);
        }

        public void set(O instance, T value) {
            super.set(instance, value);
        }
    }
}
