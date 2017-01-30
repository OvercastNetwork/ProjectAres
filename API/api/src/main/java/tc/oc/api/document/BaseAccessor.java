package tc.oc.api.document;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.reflect.TypeToken;
import tc.oc.api.docs.virtual.Document;
import tc.oc.commons.core.reflect.Types;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class BaseAccessor<T> implements Accessor<T> {

    private final DocumentRegistry registry;
    private DocumentMeta<?> document;
    private @Nullable Accessor<?> override;

    protected BaseAccessor(DocumentRegistry registry) {
        this.registry = checkNotNull(registry);
    }

    @Override
    public DocumentMeta<?> document() {
        if(document == null) {
            this.document = registry.getMeta((Class<? extends Document>) member().getDeclaringClass());

            Accessor<?> override = null;
            for(DocumentMeta<?> ancestor : document.ancestors()) {
                if(ancestor != document) {
                    override = getOverrideIn(ancestor);
                    if(override != null) break;
                }
            }
            this.override = override;
        }
        return document;
    }

    @Override
    public String name() {
        return member().getName();
    }

    @Override
    public Type resolvedType(Type documentType) {
        return resolvedType(TypeToken.of(documentType));
    }

    @Override
    public Type resolvedType(TypeToken documentType) {
        return documentType.resolveType(type()).getType();
    }

    @Override
    public @Nullable Accessor<?> override() {
        document();
        return override;
    }

    protected abstract @Nullable Accessor<?> getOverrideIn(DocumentMeta<?> ancestor);

    @Override
    public boolean isPrimitive() {
        final Type type = type();
        return type instanceof Class && ((Class) type).isPrimitive();
    }

    @Override public Class<T> boxType() {
        return isPrimitive() ? Types.box(rawType())
                             : rawType();
    }

    @Override
    public boolean isNullable() {
        if(isPrimitive()) return false;

        {
            final AccessibleObject member = member();
            if(member.getAnnotation(Nullable.class) != null) return true;
            if(member.getAnnotation(Nonnull.class) != null) return false;
        }
        {
            final Member member = member();
            if(member.getDeclaringClass().getAnnotation(Nullable.class) != null) return true;
            if(member.getDeclaringClass().getAnnotation(Nonnull.class) != null) return false;
        }

        if(override() != null) return override().isNullable();

        return true;
    }

    @Override
    public boolean hasDefault() {
        return isNullable() || isImplemented(document().type()) || isImplemented(document().baseType());
    }

    @Override
    public T validate(T value) {
        if(value == null) {
            if(!isNullable()) {
                throw new NullPointerException("null value for non-nullable property " + name());
            }
        } else {
            if(!boxType().isInstance(value)) {
                throw new ClassCastException("value of type " + value.getClass().getName() +
                                             " is not assignable to property " + name() + " of type " + rawType().getName());
            }
        }

        return value;
    }
}
