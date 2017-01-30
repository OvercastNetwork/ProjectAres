package tc.oc.api.document;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import javax.annotation.Nullable;

import com.google.common.reflect.TypeToken;
import tc.oc.api.docs.virtual.Document;

/**
 * Metadata for an accessor of a {@link Document} property of type {@link T}.
 * This object wraps a particular field or method declared in a particular
 * class. A property can have multiple accessors e.g. if they override each other.
 */
public interface Accessor<T> {

    /**
     * Metadata of the {@link Document} that declares this property
     */
    DocumentMeta<?> document();

    /**
     * Serialized name of the property
     */
    String name();

    /**
     * Generic type of the property
     */
    Type type();

    boolean isPrimitive();

    /**
     * Type of the property in the context of the given document type.
     * If this property type depends on type parameters declared on the
     * document, they will be resolved using the actual type arguments
     * of the given document type.
     *
     * For example, if a document is declared {@code Doc<T>} and it has a
     * property {@code List<T> things;}, then calling this method on the
     * things accessor with argument {@code Doc<String>} would return
     * {@code List<String>}.
     */
    Type resolvedType(Type documentType);

    Type resolvedType(TypeToken documentType);

    /**
     * Raw type of the property
     */
    Class<T> rawType();

    Class<T> boxType();

    /**
     * Java reflection API handle for the wrapped accessor
     */
    <M extends AccessibleObject & Member> M member();

    /**
     * Accessor for the same property from an ancestor document that this accessor overrides.
     */
    @Nullable Accessor<?> override();

    /**
     * Is the property allowed to be null? This is determined from @Nullable and @Nonnull
     * annotations on the wrapped member. Overrides inherit the nullability of their parent,
     * and can override it with their own annotation. Nulls are allowed by default if no
     * annotations are present in the property ancestry.
     */
    boolean isNullable();

    /**
     * Is this accessor implemented in the given class? This is true only if
     * the wrapped member exists on the given class, and is not an abstract method.
     */
    boolean isImplemented(Class<?> type);

    boolean hasDefault();

    T validate(T value);
}
