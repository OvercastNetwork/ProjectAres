package tc.oc.commons.core.reflect;

import com.google.common.reflect.TypeToken;
import com.google.inject.TypeLiteral;

public class TypeResolver {

    private final com.google.common.reflect.TypeResolver resolver;

    public TypeResolver() {
        this(new com.google.common.reflect.TypeResolver());
    }

    public TypeResolver(com.google.common.reflect.TypeResolver resolver) {
        this.resolver = resolver;
    }

    public <T> TypeResolver where(Class<T> variable, Class<? extends T> actual) {
        return new TypeResolver(resolver.where(variable, actual));
    }

    public <T> TypeResolver where(TypeToken<T> variable, TypeToken<? extends T> actual) {
        return new TypeResolver(resolver.where(variable.getType(), actual.getType()));
    }

    public <T> TypeResolver where(TypeParameter<T> variable, TypeToken<? extends T> actual) {
        return new TypeResolver(resolver.where(variable, actual.getType()));
    }

    public <T> TypeResolver where(TypeLiteral<T> variable, TypeLiteral<? extends T> actual) {
        return new TypeResolver(resolver.where(variable.getType(), actual.getType()));
    }

    public <T> TypeResolver where(TypeParameter<T> variable, TypeLiteral<? extends T> actual) {
        return new TypeResolver(resolver.where(variable, actual.getType()));
    }

    public <T> TypeLiteral<T> resolve(TypeLiteral<T> type) {
        return (TypeLiteral<T>) TypeLiteral.get(resolver.resolveType(type.getType()));
    }

    public <T> TypeToken<T> resolve(TypeToken<T> type) {
        return (TypeToken<T>) TypeToken.of(resolver.resolveType(type.getType()));
    }
}
