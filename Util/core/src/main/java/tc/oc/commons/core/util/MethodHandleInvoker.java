package tc.oc.commons.core.util;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.inject.Provider;

import tc.oc.commons.core.proxy.MethodHandleDispatcherBase;

/**
 * Forwards all method calls to the object returned from {@link #targetFor(Method)},
 * through a bound {@link MethodHandle}. The handle is created (through
 * reflection) and bound every time a method is called, which is inefficient
 * and defeats the purpose of using handles in the first place. Subclasses
 * implement caching at different points in the call process.
 */
public abstract class MethodHandleInvoker extends MethodHandleDispatcherBase implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return dispatch(proxy, method, args, () -> { throw new NoSuchMethodError(); });
    }

    public static MethodHandleInvoker dynamic(Provider<?> targeter) {
        return new MethodHandleInvoker() {
            @Override protected Object targetFor(Method method) {
                return targeter.get();
            }
        };
    }

    public static MethodHandleInvoker caching(Supplier<?> targeter) {
        return new MethodHandleInvoker() {
            private @Nullable Object cache;
            @Override protected Object targetFor(Method method) {
                if(cache == null) {
                    cache = targeter.get();
                }
                return cache;
            }
        };
    }

    public static MethodHandleInvoker caching(Provider<?> targeter) {
        return new MethodHandleInvoker() {
            private @Nullable Object cache;
            @Override protected Object targetFor(Method method) {
                if(cache == null) {
                    cache = targeter.get();
                }
                return cache;
            }
        };
    }
}
