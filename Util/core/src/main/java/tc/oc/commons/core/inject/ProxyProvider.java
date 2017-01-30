package tc.oc.commons.core.inject;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.inject.TypeLiteral;
import tc.oc.commons.core.util.Lazy;
import tc.oc.commons.core.util.ProxyUtils;

public class ProxyProvider<T> implements Provider<T> {

    final Lazy<T> proxy;

    @Inject
    ProxyProvider(TypeLiteral<T> type, Provider<T> provider) {
        proxy = Lazy.from(
            () -> ProxyUtils.newProviderProxy((Class<T>) type.getRawType(), provider)
        );
    }

    @Override
    public T get() {
        return proxy.get();
    }
}
