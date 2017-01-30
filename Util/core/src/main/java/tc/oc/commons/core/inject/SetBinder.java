package tc.oc.commons.core.inject;

import java.util.Set;
import javax.annotation.Nullable;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.Multibinder;
import tc.oc.commons.core.reflect.ResolvableType;
import tc.oc.commons.core.reflect.TypeArgument;
import tc.oc.commons.core.reflect.Types;

/**
 * Handy wrapper around a {@link Multibinder}
 */
public class SetBinder<T> {

    private final Binder binder;
    private final TypeLiteral<T> elementType;
    private final TypeLiteral<Set<T>> collectionType;
    private final Multibinder<T> multibinder;

    protected SetBinder(Binder binder, @Nullable TypeLiteral<T> type) {
        this(binder, type == null ? null : Key.get(type));
    }

    protected SetBinder(Binder binder, @Nullable Key<T> key) {
        if(key == null) {
            key = Key.get(new ResolvableType<T>(){}.in(getClass()));
        }

        this.binder = binder.skipSources(SetBinder.class);
        this.elementType = key.getTypeLiteral();
        this.collectionType = new ResolvableType<Set<T>>(){}.with(new TypeArgument<T>(this.elementType){});
        this.multibinder = Multibinder.newSetBinder(binder, key);
    }

    /**
     * Resolve the element type using the class of this object. This will only work
     * with a subclass of {@link SetBinder} that specifies {@link T}, hence this
     * constructor is protected.
     *
     * @throws IllegalArgumentException if the element type cannot be resolved
     */
    protected SetBinder(Binder binder) {
        this(binder, (Key) null);
    }

    /**
     * Create a new SetBinder with an explicit element type (which must be fully specified)
     *
     * @throws IllegalArgumentException if the element type is not fully specified
     */
    public static <E> SetBinder<E> ofType(Binder binder, TypeLiteral<E> elementType) {
        return new SetBinder<>(binder, Types.assertFullySpecified(elementType));
    }

    /**
     * Create a new SetBinder, resolving the element type {@link E} in the context of
     * the given class.
     *
     * @throws IllegalArgumentException if the element type cannot be resolved
     */
    public static <E> SetBinder<E> inContext(Binder binder, Class<?> declaringClass) {
        return new SetBinder<>(binder, new ResolvableType<E>(){}.in(declaringClass));
    }

    public TypeLiteral<T> elementType() {
        return elementType;
    }

    public TypeLiteral<Set<T>> collectionType() {
        return collectionType;
    }

    protected Binder binder() { return binder; }

    public LinkedBindingBuilder<T> addBinding() {
        return multibinder.addBinding();
    }
}
