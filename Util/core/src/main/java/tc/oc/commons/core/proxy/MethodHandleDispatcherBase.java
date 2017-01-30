package tc.oc.commons.core.proxy;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import javax.annotation.Nullable;

import com.google.common.cache.LoadingCache;
import tc.oc.commons.core.reflect.MethodHandleUtils;
import tc.oc.commons.core.util.CacheUtils;

public abstract class MethodHandleDispatcherBase {

    private static final MethodHandle NULL_HANDLE = MethodHandles.constant(Object.class, new Object());

    private static final LoadingCache<Method, MethodHandle> UNBOUND_CACHE = CacheUtils.newCache(MethodHandleUtils::privateUnreflect);

    protected abstract @Nullable Object targetFor(Method method);

    protected MethodHandle unboundMethodHandle(Method method) {
        return UNBOUND_CACHE.getUnchecked(method);
    }

    protected MethodHandle boundMethodHandle(Method method) {
        final Object target = targetFor(method);
        return target == null ? NULL_HANDLE : unboundMethodHandle(method).bindTo(target);
    }

    public final Object dispatch(Object proxy, Method method, Object[] args, DefaultDispatch defaultDispatch) throws Throwable {
        final MethodHandle handle = boundMethodHandle(method);
        return handle == NULL_HANDLE ? defaultDispatch.call()
                                     : handle.invokeWithArguments(args);
    }

    protected interface DefaultDispatch {
        Object call() throws Throwable;
    }
}
