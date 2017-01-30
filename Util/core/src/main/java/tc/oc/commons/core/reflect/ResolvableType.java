package tc.oc.commons.core.reflect;

import java.lang.reflect.TypeVariable;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeResolver;
import com.google.common.reflect.TypeToken;
import com.google.inject.TypeLiteral;

/**
 * Extends {@link TypeLiteral} with some methods used to resolve generic types.
 */
public abstract class ResolvableType<T> extends TypeLiteral<T> {

    /**
     * Fully resolve this type in the context of the given type
     */
    public TypeLiteral<T> in(Class<?> declaringClass) {
        return Types.assertFullySpecified(Types.resolve(this, declaringClass));
    }

    /**
     * Fully resolve this type by substituting this type's formal type
     * parameters with the given actual type arguments
     */
    public TypeLiteral<T> with(TypeArgument<?>... arguments) {
        TypeToken<T> token = Types.toToken(this);
        for(TypeArgument arg : arguments) {
            token = token.where(arg, arg.actual());
        }
        return Types.assertFullySpecified(Types.toLiteral(token));
    }

    public <X> TypeLiteral<T> where(TypeParameter<X> parameter, TypeLiteral<X> type) {
        return where(parameter, Types.toToken(type));
    }

    public <X> TypeLiteral<T> where(TypeParameter<X> parameter, TypeToken<X> type) {
        return Types.toLiteral(Types.assertFullySpecified(Types.toToken(this).where(parameter, type)));
    }

    public <X> TypeLiteral<T> where(tc.oc.commons.core.reflect.TypeParameter<X> parameter, TypeLiteral<X> type) {
        return where(parameter.typeVariable(), type);
    }

    public <X> TypeLiteral<T> where(tc.oc.commons.core.reflect.TypeParameter<X> parameter, TypeToken<X> type) {
        return where(parameter.typeVariable(), type);
    }

    public TypeLiteral<T> where(String name, TypeLiteral<?> type) {
        return where(Types.typeVariable(getRawType(), name), type);
    }

    public TypeLiteral<T> where(String name, TypeToken<?> type) {
        return where(Types.typeVariable(getRawType(), name), type);
    }

    public TypeLiteral<T> where(TypeVariable<?> typeVariable, TypeLiteral<?> type) {
        final TypeResolver resolver = new TypeResolver().where(typeVariable, type.getType());
        return (TypeLiteral<T>) TypeLiteral.get(resolver.resolveType(getType()));
    }

    public TypeLiteral<T> where(TypeVariable<?> typeVariable, TypeToken<?> type) {
        final TypeResolver resolver = new TypeResolver().where(typeVariable, type.getType());
        return (TypeLiteral<T>) TypeLiteral.get(resolver.resolveType(getType()));
    }

    /**
     * Fully resolve the given type in the context of this type
     */
    public <U> TypeLiteral<U> resolve(TypeLiteral<U> type) {
        return Types.assertFullySpecified(Types.resolve(type, getRawType()));
    }
}
