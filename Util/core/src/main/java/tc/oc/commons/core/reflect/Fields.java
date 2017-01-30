package tc.oc.commons.core.reflect;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import com.google.common.reflect.TypeToken;

public final class Fields {
    private Fields() {}

    public static String descriptor(Field field) {
        return Types.descriptor(field.getType());
    }

    public static Field needAssignableField(boolean privateAccess, Class<?> parent, Class<?> type, String name) throws NoSuchFieldException {
        Field field = null;

        if(privateAccess) {
            try {
                field = parent.getDeclaredField(name);
            } catch(NoSuchFieldException ignored) {}
        }

        if(field == null) {
            try {
                field = parent.getField(name);
            } catch(NoSuchFieldException ignored) {}
        }

        if(field == null) {
            throw new NoSuchFieldException("No field named " + name);
        }

        if(!field.getType().isAssignableFrom(type)) {
            throw new NoSuchFieldException("Field " + parent.getName() + "." + name +
                                           " with type " + field.getType().getName() +
                                           " is not assignable from " + type.getName());
        }

        return field;
    }

    private static Field assertAssignableTo(Field field, TypeToken<?> type) {
        if(!type.isAssignableFrom(TypeToken.of(field.getGenericType()))) {
            throw new NoSuchFieldError("Field " + field.getName() + " is not of type " + type);
        }
        return field;
    }

    public static <T> T read(Class<?> parent, Class<T> type, String name, @Nullable Object obj) {
        return read(parent, TypeToken.of(type), name, obj);
    }

    public static <T> T read(Class<?> parent, TypeToken<T> type, String name, @Nullable Object obj) {
        return read(named(parent, name), type, obj);
    }

    public static <T> T read(Class<?> parent, TypeToken<T> type, @Nullable Object obj) {
        return (T) readFieldUnchecked(oneOfType(parent, type), obj);
    }

    public static <T> T read(Field field, TypeToken<T> type, @Nullable Object obj) {
        assertAssignableTo(field, type);
        return (T) readFieldUnchecked(field, obj);
    }

    public static Object readFieldUnchecked(Field field, @Nullable Object obj) {
        final boolean wasAccessible = field.isAccessible();
        try {
            field.setAccessible(true);
            return field.get(obj);
        } catch(IllegalAccessException e) {
            throw new NoSuchFieldError("Cannot access field " + field.getName());
        } finally {
            field.setAccessible(wasAccessible);
        }
    }

    public static void writeField(@Nullable Object obj, Field field, Object value) {
        final boolean wasAccessible = field.isAccessible();
        try {
            field.setAccessible(true);
            field.set(obj, value);
        } catch(IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            field.setAccessible(wasAccessible);
        }
    }

    public static void writeField(@Nullable Object obj, String name, Object value) {
        final Field field;
        try {
            field = obj.getClass().getDeclaredField(name);
        } catch(NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        final boolean wasAccessible = field.isAccessible();
        try {
            field.setAccessible(true);
            field.set(obj, value);
        } catch(IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            field.setAccessible(wasAccessible);
        }
    }

    public static Field named(Class<?> decl, String name) {
        try {
            return decl.getDeclaredField(name);
        } catch(NoSuchFieldException e) {
            try {
                return decl.getField(name);
            } catch(NoSuchFieldException e1) {
                throw new NoSuchFieldError("No field named \"" + name + "\" accessible from " + decl.getName());
            }
        }
    }

    public static Stream<Field> accessibleFrom(Class<?> decl) {
        return Stream.concat(Stream.of(decl.getDeclaredFields()),
                             Stream.of(decl.getFields()))
                     .distinct();
    }

    public static Predicate<Field> ofType(TypeToken<?> type) {
        return field -> TypeToken.of(field.getGenericType()).equals(type);
    }

    public static Predicate<Field> assignableFrom(TypeToken<?> type) {
        return field -> TypeToken.of(field.getGenericType()).isAssignableFrom(type);
    }

    public static Predicate<Field> assignableTo(TypeToken<?> type) {
        return field -> type.isAssignableFrom(field.getGenericType());
    }

    public static Stream<Field> ofType(Class<?> decl, TypeToken<?> type) {
        return accessibleFrom(decl).filter(ofType(type));
    }

    public static Stream<Field> assignableFrom(Class<?> decl, TypeToken<?> type) {
        return accessibleFrom(decl).filter(assignableFrom(type));
    }

    public static Stream<Field> assignableTo(Class<?> decl, TypeToken<?> type) {
        return accessibleFrom(decl).filter(assignableTo(type));
    }

    private static Field one(Stream<Field> fields, Class<?> decl, TypeToken<?> type, String description) {
        final List<Field> list = fields.collect(Collectors.toList());
        switch(list.size()) {
            case 0: throw new NoSuchFieldError("No field " + description + " " + type +
                                               " in " + decl.getName());
            case 1: return list.get(0);
            default: throw new NoSuchFieldError("Multiple fields " + description + " " + type +
                                                " in " + decl.getName() +
                                                ": " + list.stream().map(Field::getName).collect(Collectors.joining(", ")));
        }
    }

    public static Field oneOfType(Class<?> decl, TypeToken<?> type) {
        return one(ofType(decl, type), decl, type, "of type");
    }

    public static Field oneAssignableFrom(Class<?> decl, TypeToken<?> type) {
        return one(assignableFrom(decl, type), decl, type, "assignable from");
    }

    public static Field oneAssignableTo(Class<?> decl, TypeToken<?> type) {
        return one(assignableTo(decl, type), decl, type, "assignable to");
    }


    public static Stream<Field> declaredInAncestors(Class<?> klass) {
        return Types.ancestors(klass)
                    .stream()
                    .flatMap(ancestor -> Stream.of(ancestor.getDeclaredFields()))
                    .distinct();
    }
}
