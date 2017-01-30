package tc.oc.commons.core.reflect;

import java.lang.reflect.TypeVariable;

import com.google.common.cache.LoadingCache;
import com.google.common.reflect.TypeToken;
import com.google.inject.TypeLiteral;
import tc.oc.commons.core.util.CacheUtils;

public class TypeParameterCache<D, V> {

    private final LoadingCache<Class<? extends D>, TypeToken<V>> cache;

    public TypeParameterCache(Class<D> decl, String name) {
        this(Types.typeVariable(decl, name));
    }

    public TypeParameterCache(TypeVariable<Class<D>> typeVariable) {
        cache = CacheUtils.newCache(
            context -> (TypeToken<V>) Types.assertFullySpecified(TypeToken.of(context).resolveType(typeVariable))
        );
    }

    public TypeToken<V> resolve(Class<? extends D> context) {
        return cache.getUnchecked(context);
    }

    public TypeToken<V> resolve(TypeToken<? extends D> context) {
        return cache.getUnchecked((Class<? extends D>) context.getRawType());
    }

    public TypeLiteral<V> resolve(TypeLiteral<? extends D> context) {
        return Types.toLiteral(cache.getUnchecked((Class<? extends D>) context.getRawType()));
    }

    public Class<V> resolveRaw(Class<? extends D> context) {
        return (Class<V>) resolve(context).getRawType();
    }
}
