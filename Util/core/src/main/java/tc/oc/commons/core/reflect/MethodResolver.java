package tc.oc.commons.core.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.stream.Stream;
import javax.annotation.Nullable;

/**
 * Convenience wrapper around some JDK invoke stuff
 */
public class MethodResolver {

    private final MethodHandles.Lookup lookup;

    public MethodResolver(MethodHandles.Lookup lookup) {
        this.lookup = lookup;
    }

    public MethodResolver(Class<?> from) {
        this(MethodHandleUtils.privateLookup(from));
    }

    public MethodResolver() {
        this(MethodHandles.publicLookup());
    }

    public MethodHandles.Lookup lookup() {
        return lookup;
    }

    public Stream<MethodHandle> methodHandles(Class<?> target) {
        return Methods.declaredMethodsInAncestors(target)
                      .map(method -> {
                          try {
                              return lookup.unreflect(method);
                          } catch(IllegalAccessException e) {
                              return null;
                          }
                      }).filter(h -> h != null);
    }

    public MethodHandle virtualHandle(Class<?> target, String name, MethodType type) throws NoSuchMethodException, IllegalAccessException {
        return lookup.findVirtual(target, name, type);
    }

    public MethodHandle virtualHandle(Class<?> target, Method called) throws NoSuchMethodException, IllegalAccessException {
        return lookup.findVirtual(target, called.getName(), Methods.methodType(called));
    }

    public @Nullable MethodHandle tryVirtualHandle(Class<?> target, String name, MethodType type) {
        try {
            return virtualHandle(target, name, type);
        } catch(NoSuchMethodException | IllegalAccessException e) {
            return null;
        }
    }

    public @Nullable MethodHandle tryVirtualHandle(Class<?> target, Method called) {
        return tryVirtualHandle(target, called.getName(), Methods.methodType(called));
    }

    public MethodHandleInfo virtualInfo(Class<?> target, String name, MethodType type) throws NoSuchMethodException, IllegalAccessException {
        return lookup.revealDirect(virtualHandle(target, name, type));
    }

    public MethodHandleInfo virtualInfo(Class<?> target, Method called) throws NoSuchMethodException, IllegalAccessException {
        return virtualInfo(target, called.getName(), Methods.methodType(called));
    }

    public boolean hasMethod(Class<?> target, String name, MethodType type) {
        try {
            virtualInfo(target, name, type);
            return true;
        } catch(NoSuchMethodException | IllegalAccessException e) {
            return false;
        }
    }

    public boolean hasMethod(Class<?> target, Method called) {
        return hasMethod(target, called.getName(), Methods.methodType(called));
    }
}
