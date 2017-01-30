package tc.oc.commons.core.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.Invokable;
import com.google.common.reflect.TypeToken;
import com.google.inject.TypeLiteral;
import tc.oc.commons.core.util.ExceptionUtils;
import tc.oc.commons.core.util.ProxyUtils;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Given an interface and a target class, these methods generate a proxy object used to
 * access methods and fields in the target class, bypassing visibility and access restrictions.
 * The names and signatures of the methods in the proxy interface are used to resolve
 * their respective members in the target class, and strict type checking is performed.
 *
 * When you need to access private members in a 3rd party class, this is somewhat
 * cleaner and safer than using reflection and dynamic invocation directly. If you ensure
 * that the proxy is created at application startup, then you will know right away if the
 * target member changes.
 */
public class Delegates {

    public static <T> T newStaticMethodDelegate(TypeToken<T> proxyType, Class<?> targetType) {
        return ProxyUtils.newProxy((Class<T>) proxyType.getRawType(),
                                   new StaticMethodDelegate<>(proxyType, targetType));
    }

    public static <T> T newStaticMethodDelegate(TypeLiteral<T> proxyType, Class<?> targetType) {
        return newStaticMethodDelegate(Types.toToken(proxyType), targetType);
    }

    public static <T> T newStaticMethodDelegate(Class<T> proxyType, Class<?> targetType) {
        return newStaticMethodDelegate(TypeToken.of(proxyType), targetType);
    }

    public static <T> T newStaticFieldDelegate(TypeToken<T> proxyType, Class<?> targetType) {
        return ProxyUtils.newProxy((Class<T>) proxyType.getRawType(),
                                   new StaticFieldDelegate<>(proxyType, targetType));
    }

    public static <T> T newStaticFieldDelegate(TypeLiteral<T> proxyType, Class<?> targetType) {
        return newStaticFieldDelegate(Types.toToken(proxyType), targetType);
    }

    public static <T> T newStaticFieldDelegate(Class<T> proxyType, Class<?> targetType) {
        return newStaticFieldDelegate(TypeToken.of(proxyType), targetType);
    }

    public static <T> T newConstructorDelegate(TypeToken<T> proxyType, Class<?> targetType) {
        return ProxyUtils.newProxy((Class<T>) proxyType.getRawType(),
                                   new ConstructorDelegate<>(proxyType, targetType));
    }

    public static <T> T newConstructorDelegate(TypeLiteral<T> proxyType, Class<?> targetType) {
        return newConstructorDelegate(Types.toToken(proxyType), targetType);
    }

    public static <T> T newConstructorDelegate(Class<T> proxyType, Class<?> targetType) {
        return newConstructorDelegate(TypeToken.of(proxyType), targetType);
    }
}

abstract class BaseDelegate<T> implements InvocationHandler {

    final Class<?> targetType;
    final MethodHandles.Lookup lookup;
    final ImmutableMap<Method, MethodHandle> map;

    BaseDelegate(TypeToken<T> proxyType, Class<?> targetType) {
        this.targetType = targetType;
        this.lookup = MethodHandleUtils.privateLookup(targetType);

        final Class<T> rawProxyType = (Class<T>) proxyType.getRawType();
        checkArgument(rawProxyType.isInterface());

        final ImmutableMap.Builder<Method, MethodHandle> builder = ImmutableMap.builder();

        for(Method rawProxyMethod : rawProxyType.getMethods()) {
            final Invokable<T, ?> proxyMethod = proxyType.method(rawProxyMethod);
            try {
                builder.put(rawProxyMethod, createHandle(rawProxyMethod, proxyMethod));
            } catch(NoSuchMethodException e) {
                throw new NoSuchMethodError(missingError(proxyMethod));
            } catch(NoSuchFieldException e) {
                throw new NoSuchFieldError(missingError(proxyMethod));
            } catch(ReflectiveOperationException e) {
                throw ExceptionUtils.propagate(e);
            }
        }

        this.map = builder.build();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return map.get(method).invokeWithArguments(args);
    }

    abstract String missingError(Invokable<T, ?> proxyMethod);

    abstract MethodHandle createHandle(Method rawProxyMethod, Invokable<T, ?> proxyMethod) throws ReflectiveOperationException;
}

class ConstructorDelegate<T> extends BaseDelegate<T> {
    ConstructorDelegate(TypeToken<T> proxyType, Class<?> targetType) {
        super(proxyType, targetType);
    }

    @Override
    String missingError(Invokable<T, ?> proxyMethod) {
        return "Target class " + targetType.getName() +
               " has no constructor matching " + proxyMethod;
    }

    @Override
    MethodHandle createHandle(Method rawProxyMethod, Invokable<T, ?> proxyMethod) throws ReflectiveOperationException {
        if(!proxyMethod.getReturnType().getRawType().isAssignableFrom(targetType)) {
            throw new MethodFormException(rawProxyMethod, "Constructor delegate must return target type " + targetType.getName());
        }

        // findConstructor requires the return type to be void
        return lookup.findConstructor(targetType,
                                      Methods.methodType(proxyMethod)
                                             .changeReturnType(void.class));
    }
}

class StaticMethodDelegate<T> extends BaseDelegate<T> {
    StaticMethodDelegate(TypeToken<T> proxyType, Class<?> targetType) {
        super(proxyType, targetType);
    }

    @Override
    String missingError(Invokable<T, ?> proxyMethod) {
        return "Target class " + targetType.getName() +
               " has no static method matching " + proxyMethod;
    }

    @Override
    MethodHandle createHandle(Method rawProxyMethod, Invokable<T, ?> proxyMethod) throws ReflectiveOperationException {
        return lookup.findStatic(targetType,
                                 proxyMethod.getName(),
                                 Methods.methodType(proxyMethod));
    }
}

class StaticFieldDelegate<T> extends BaseDelegate<T> {
    StaticFieldDelegate(TypeToken<T> proxyType, Class<?> targetType) {
        super(proxyType, targetType);
    }

    @Override
    String missingError(Invokable<T, ?> proxyMethod) {
        return "Target class " + targetType.getName() +
               " has no static field matching " + proxyMethod;
    }

    @Override
    MethodHandle createHandle(Method rawProxyMethod, Invokable<T, ?> proxyMethod) throws ReflectiveOperationException {
        if(proxyMethod.getReturnType().getRawType().equals(void.class)) {
            if(proxyMethod.getParameters().size() == 1) {
                return lookup.findStaticSetter(targetType,
                                               proxyMethod.getName(),
                                               proxyMethod.getParameters().get(0).getType().getRawType());
            }
        } else {
            if(proxyMethod.getParameters().isEmpty()) {
                return lookup.findStaticGetter(targetType,
                                               proxyMethod.getName(),
                                               proxyMethod.getReturnType().getRawType());
            }
        }

        throw new MethodFormException(rawProxyMethod, "Field delegate method does not have a getter or setter signature");
    }
}
