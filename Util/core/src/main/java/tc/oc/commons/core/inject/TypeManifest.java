package tc.oc.commons.core.inject;

import javax.annotation.Nullable;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeResolver;
import com.google.common.reflect.TypeToken;
import com.google.inject.TypeLiteral;
import tc.oc.commons.core.reflect.TypeArgument;
import tc.oc.commons.core.reflect.Types;

/**
 * Base for manifests that configure things related to a single generic type {@link T}
 *
 * This simply provides some commonly useful functionality to subclasses:
 *
 *  - Ensures the type parameter is fully specified, resolving it automatically if possible
 *  - Provides a few helpful fields related to {@link T}
 *  - Provides {@link #resolve(TypeLiteral)} to conveniently resolve other types that refer to {@link T}
 *  - Uses the type as the equality key for the manifest (see {@link KeyedManifest})
 */
public class TypeManifest<T> extends KeyedManifest {

    protected final TypeLiteral<T> type;
    protected final TypeToken<T> typeToken;
    protected final TypeParameter<T> typeParameter;
    protected final TypeArgument<T> typeArg;

    private final TypeResolver resolver;

    protected TypeManifest() {
        this(null);
    }

    public TypeManifest(@Nullable TypeLiteral<T> nullableType) {
        this.typeToken = nullableType != null ? Types.toToken(nullableType)
                                              : new TypeToken<T>(getClass()){};
        this.type = nullableType != null ? nullableType
                                         : Types.toLiteral(typeToken);

        Types.assertFullySpecified(this.type);

        this.typeParameter = new TypeParameter<T>(){};
        this.typeArg = new TypeArgument<T>(this.type){};

        TypeResolver resolver = new TypeResolver();
        for(Class<?> cls = getClass(); cls != null; cls = cls.getSuperclass()) {
            if(cls.getTypeParameters().length > 0) {
                resolver = resolver.where(cls.getTypeParameters()[0], type.getType());
            }
        }
        this.resolver = resolver;
    }

    @Override
    protected Object manifestKey() {
        return type;
    }

    /**
     * Try to resolve variables in the given type using the known value of {@link T}
     *
     * TODO: This method makes the rather naive assumption that the first formal type
     * parameter of every class descended from {@link TypeManifest} is the same type
     * as {@link T}, which is typical but by no means guaranteed. The resolution could
     * be much smarter with a bit more reflection work.
     */
    protected <R> TypeLiteral<R> resolve(TypeLiteral<R> related) {
        return (TypeLiteral<R>) TypeLiteral.get(Types.assertFullySpecified(resolver.resolveType(related.getType())));
    }
}
