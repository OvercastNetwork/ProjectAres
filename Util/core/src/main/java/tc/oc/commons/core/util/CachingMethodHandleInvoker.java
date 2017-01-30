package tc.oc.commons.core.util;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import com.google.common.base.Throwables;
import com.google.common.cache.Cache;

/**
 * Forwards all method calls to the object returned from {@link #targetFor(Method)},
 * through cached {@link MethodHandle}s. The handles are bound to the target
 * object at creation time, so the return value of {@link #targetFor(Method)} must
 * remain constant for any given {@link Method}.
 */
public abstract class CachingMethodHandleInvoker extends MethodHandleInvoker {

    private final Cache<Method, MethodHandle> cache = CacheUtils.newBuilder().build();

    @Override
    protected MethodHandle boundMethodHandle(Method method) {
        try {
            return cache.get(method, () -> super.boundMethodHandle(method));
        } catch(ExecutionException e) {
            throw Throwables.propagate(e);
        }
    }

    public static CachingMethodHandleInvoker caching(Function<? super Method, Object> targeter) {
        return new CachingMethodHandleInvoker() {
            @Override protected Object targetFor(Method method) {
                return targeter.apply(method);
            }
        };
    }
}
