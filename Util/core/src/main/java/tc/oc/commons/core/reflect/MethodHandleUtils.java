package tc.oc.commons.core.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.google.common.base.Throwables;
import com.google.common.cache.LoadingCache;
import tc.oc.commons.core.util.CacheUtils;

public final class MethodHandleUtils {
    private MethodHandleUtils() {}

    private static final Constructor<MethodHandles.Lookup> LOOKUP_CONSTRUCTOR;
    static {
        try {
            LOOKUP_CONSTRUCTOR = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
            if(!LOOKUP_CONSTRUCTOR.isAccessible()) {
                LOOKUP_CONSTRUCTOR.setAccessible(true);
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private static final LoadingCache<Class<?>, MethodHandles.Lookup> PRIVATE_LOOKUP_CACHE = CacheUtils.newCache(declaringClass -> {
        try {
            return LOOKUP_CONSTRUCTOR.newInstance(declaringClass, MethodHandles.Lookup.PRIVATE |
                                                                  MethodHandles.Lookup.PROTECTED |
                                                                  MethodHandles.Lookup.PACKAGE |
                                                                  MethodHandles.Lookup.PUBLIC);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    });

    /**
     * Returns a {@link MethodHandles.Lookup} that doesn't do "access" checks (since it allows private calls).
     * A work-around for Proxy classes to access "default" interface methods.
     *
     * See http://rmannibucau.wordpress.com/2014/03/27/java-8-default-interface-methods-and-jdk-dynamic-proxies/
     */
    public static MethodHandles.Lookup privateLookup(Class<?> declaringClass) {
        return PRIVATE_LOOKUP_CACHE.getUnchecked(declaringClass);
    }

    public static MethodHandle privateUnreflect(Class<?> declaringClass, Method method) {
        try {
            return privateLookup(declaringClass).unreflect(method);
        } catch(IllegalAccessException e) {
            throw Throwables.propagate(e); // impossible
        }
    }

    public static MethodHandle privateUnreflect(Method method) {
        return privateUnreflect(method.getDeclaringClass(), method);
    }

    public static MethodHandle privateUnreflectGetter(Class<?> declaringClass, Field field) {
        try {
            return privateLookup(declaringClass).unreflectGetter(field);
        } catch(IllegalAccessException e) {
            throw Throwables.propagate(e); // impossible
        }
    }

    public static MethodHandle privateUnreflectGetter(Field field) {
        return privateUnreflectGetter(field.getDeclaringClass(), field);
    }

    /**
     * Returns a {@link MethodHandle} to the specified interface default-method
     */
    public static MethodHandle defaultMethodHandle(Method method) {
        final Class<?> declaringClass = method.getDeclaringClass();
        try {
            return privateLookup(declaringClass).unreflectSpecial(method, declaringClass);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
