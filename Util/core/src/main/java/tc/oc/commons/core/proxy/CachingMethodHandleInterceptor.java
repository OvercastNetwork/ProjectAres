package tc.oc.commons.core.proxy;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;

import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import tc.oc.commons.core.util.CacheUtils;

public abstract class CachingMethodHandleInterceptor extends MethodHandleInterceptor {

    private final Cache<Method, MethodHandle> cache = CacheUtils.newBuilder().build();

    @Override
    protected MethodHandle boundMethodHandle(Method method) {
        try {
            return cache.get(method, () -> super.boundMethodHandle(method));
        } catch(ExecutionException e) {
            throw Throwables.propagate(e);
        }
    }
}
