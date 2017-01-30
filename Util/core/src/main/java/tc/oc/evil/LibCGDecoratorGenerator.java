package tc.oc.evil;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import tc.oc.commons.core.reflect.MethodResolver;
import tc.oc.commons.core.reflect.Methods;

public class LibCGDecoratorGenerator implements DecoratorGenerator {

    private static class CGMeta<T, D extends Decorator<T>> extends DecoratorGenerator.Meta<T, D> {
        final Enhancer enhancer;

        public CGMeta(Class<T> type, Class<D> decorator, Class implementation, Enhancer enhancer) {
            super(type, decorator, implementation);
            this.enhancer = enhancer;
        }

        @Override
        public D newInstance() throws Exception {
            return (D) enhancer.create();
        }

        @Override
        public D newInstance(Class[] parameterTypes, Object[] arguments) throws Exception {
            return (D) enhancer.create(parameterTypes, arguments);
        }
    }

    private static class Callback<T, D extends Decorator<T>> implements MethodInterceptor {
        final Class<T> base;
        final Class<D> decorator;
        final MethodResolver resolver;
        final Cache<Method, Boolean> isDecorated = CacheBuilder.newBuilder().build();
        final Cache<Method, MethodHandle> methodHandles = CacheBuilder.newBuilder().build();

        private Callback(Class<T> base, Class<D> decorator) {
            this.base = base;
            this.decorator = decorator;
            this.resolver = new MethodResolver(decorator);
        }

        boolean isDecorated(Method method) throws ExecutionException {
            final Boolean yes = isDecorated.getIfPresent(method);
            if(yes != null) return yes;

            synchronized(isDecorated) {
                return isDecorated.get(method, () -> {
                    // This may look simple, but it was absurdly difficult to get right.
                    // There are a lot of subtleties involved in method dispatch.
                    //
                    // Note, for example, that we completely ignore the declaring class
                    // of all methods involved. The only thing that matters is the
                    // name and signature of the method. This is absolutely necessary
                    // in order to handle all cases properly.

                    // First of all, make sure the delegate() method always goes to the decorator.
                    if("delegate".equals(method.getName()) &&
                       method.getParameterTypes().length == 0) return true;

                    // Look for a matching method in the base class (which may be abstract).
                    // If the method is completely absent from the base, send it to the decorator.
                    final Method baseMethod = Methods.accessibleMethod(base, method);
                    if(baseMethod == null) return true;

                    // Now look for a callable match in the decorator. If we find one,
                    // and it's different from the base method, and it doesn't come from
                    // an interface, then assume it's an override and call it.
                    //
                    // To be consistent with Java, we don't allow interface default methods
                    // to override methods in the base class.
                    final Method decoMethod = Methods.callableMethod(decorator, method);
                    return decoMethod != null &&
                           !decoMethod.equals(baseMethod) &&
                           !decoMethod.getDeclaringClass().isInterface();
                });
            }
        }

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            if(isDecorated(method)) {
                // Decorated method
                return proxy.invokeSuper(obj, args);
            } else {
                final T t = ((Decorator<T>) obj).delegate();
                if(method.getDeclaringClass().isInstance(t)) {
                    // Forwarded method
                    return proxy.invoke(t, args);
                } else {
                    // Forwarded method shadowed by an interface method in the decorator.
                    //
                    // This can happen if the decorator implements an interface that the
                    // base class doesn't, and that interface contains a method that shadows
                    // one on the base class. Java would allow the method to be called on the
                    // base anyway, but MethodProxy refuses to invoke it on something that
                    // is not assignable to the method's declaring type. So, unfortunately,
                    // we have to fall back to the JDK to handle this case.
                    return methodHandles.get(method, () ->
                        resolver.virtualHandle(t.getClass(), method).bindTo(t)
                    ).invokeWithArguments(args);
                }
            }
        }
    }

    @Override
    public <T, D extends Decorator<T>> DecoratorGenerator.Meta<T, D> implement(Class<T> type, Class<D> decorator) {
        final Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(decorator);
        enhancer.setCallbackType(MethodInterceptor.class);
        final Class<? extends D> impl = enhancer.createClass();
        enhancer.setCallback(new Callback<>(type, decorator));
        return new CGMeta(type, decorator, impl, enhancer);
    }
}
