package tc.oc.commons.core.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collection;
import javax.inject.Provider;

import com.google.inject.TypeLiteral;

import static com.google.common.base.Preconditions.checkNotNull;

public class ProxyUtils {
    private ProxyUtils() {}

    public static <T> T newProxy(Class<T> type, InvocationHandler handler) {
        return newProxy(type, handler, type.getClassLoader());
    }

    public static <T> T newProxy(Class<T> type, InvocationHandler handler, ClassLoader classLoader) {
        return (T) Proxy.newProxyInstance(
            checkNotNull(classLoader),
            new Class<?>[]{checkNotNull(type)},
            checkNotNull(handler)
        );
    }

    public static <T> T newProxy(Class<T> proxyType, Collection<? extends Class<?>> interfaces, InvocationHandler handler) {
        return newProxy(proxyType, interfaces, handler, proxyType.getClassLoader());
    }

    public static <T> T newProxy(Class<T> proxyType, Collection<? extends Class<?>> interfaces, InvocationHandler handler, ClassLoader classLoader) {
        // proxyType must be first in the interface list, so any method that it declares
        // will always be resolved to it, rather than a possible override in another interface.
        final Class[] array = new Class<?>[1 + interfaces.size()];
        array[0] = proxyType;
        ArrayUtils.copy(interfaces, array, 1);

        return (T) Proxy.newProxyInstance(
            checkNotNull(classLoader),
            array,
            checkNotNull(handler)
        );
    }

    /**
     * Return a generated implementation of {@link T} that delegates all method calls
     * to the instance returned by the given {@link Provider}. The provider is called
     * for every method invocation.
     *
     * The provider's {@link ClassLoader} is used to load the generated class.
     */
    public static <T> T newProviderProxy(Class<T> type, Provider<T> provider) {
        return newProxy(type, MethodHandleInvoker.dynamic(provider));
    }

    public static <T> T newProviderProxy(Class<T> type, Collection<? extends Class<?>> interfaces, Provider<T> provider) {
        return newProxy(type, interfaces, MethodHandleInvoker.dynamic(provider));
    }

    public static <T> T newProviderProxy(TypeLiteral<T> type, Provider<T> provider) {
        return newProviderProxy((Class<T>) type.getRawType(), provider);
    }

    /**
     * Return a generated implementation of {@link T} that delegates all method calls
     * to the instance returned by the given {@link Provider}. That instance is not
     * retrieved until the first time a method is called, after which the returned
     * instance is cached and reused forever.
     *
     * The provider's {@link ClassLoader} is used to load the generated class.
     */
    public static <T> T newCachingProviderProxy(Class<T> type, Provider<T> provider) {
        return newProxy(type, MethodHandleInvoker.caching(provider));
    }
}
