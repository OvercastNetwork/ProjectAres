package tc.oc.commons.core.proxy;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public abstract class MethodHandleInterceptor extends MethodHandleDispatcherBase implements MethodInterceptor {
    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        return dispatch(obj, method, args, () -> proxy.invokeSuper(obj, args));
    }
}
