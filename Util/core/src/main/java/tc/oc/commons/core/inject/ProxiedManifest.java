package tc.oc.commons.core.inject;

import com.google.inject.TypeLiteral;
import tc.oc.commons.core.reflect.ResolvableType;
import tc.oc.commons.core.reflect.TypeParameter;

/**
 * @see Proxied
 */
public class ProxiedManifest<T> extends KeyedManifest {

    private final TypeLiteral<T> type;

    public ProxiedManifest(TypeLiteral<T> type) {
        this.type = type;
    }

    @Override
    protected Object manifestKey() {
        return type;
    }

    @Override
    protected void configure() {
        if(!type.getRawType().isInterface()) {
            addError("Cannot proxy " + type + " because it is not an interface");
        }

        bind(type)
            .annotatedWith(Proxied.class)
            .toProvider(new ResolvableType<ProxyProvider<T>>(){}.where(new TypeParameter<T>(){}, type));
    }
}
