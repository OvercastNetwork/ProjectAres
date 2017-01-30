package tc.oc.commons.core.reflect;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.cache.LoadingCache;
import com.google.common.reflect.TypeToken;
import tc.oc.commons.core.util.CacheUtils;
import tc.oc.commons.core.util.Utils;

public class PatternInvoker {

    private static class InvocationKey<T, R> {
        final TypeToken<T> target;
        final String name;
        final GenericMethodType<R> signature;

        private InvocationKey(TypeToken<T> target, String name, GenericMethodType<R> signature) {
            this.target = target;
            this.name = name;
            this.signature = signature;
        }

        @Override
        public int hashCode() {
            return Objects.hash(target, name, signature);
        }

        @Override
        public boolean equals(Object obj) {
            return Utils.equals(InvocationKey.class, this, obj, that ->
                this.target.equals(that.target) &&
                this.name.equals(that.name) &&
                this.signature.equals(that.signature)
            );
        }

        Set<MethodHandle> methodHandles() {
            final MethodResolver resolver = new MethodResolver(target.getRawType());
            return Methods.declaredMethodsInAncestors(target.getRawType())
                          .filter(method -> !Members.isStatic(method) &&
                                            name.equals(method.getName()) &&
                                            signature.canInvoke(target, method))
                          .map(method -> {
                              try {
                                  return resolver.virtualHandle(target.getRawType(), method);
                              } catch(NoSuchMethodException | IllegalAccessException e) {
                                  return null;
                              }
                          })
                          .collect(Collectors.toSet());
        }
    }

    public static class InvocationHandle<T, R> {
        final InvocationKey<T, R> key;
        final Set<MethodHandle> methodHandles;

        InvocationHandle(InvocationKey<T, R> key) {
            this.key = key;
            this.methodHandles = key.methodHandles();
        }

        public List<R> invoke(T target, List<Object> args) throws Throwable {
            if(methodHandles.isEmpty()) return Collections.emptyList();

            final Object[] flatArgs = new Object[args.size() + 1];
            flatArgs[0] = target;
            for(int i = 0; i < args.size(); i++) {
                flatArgs[i + 1] = args.get(i);
            }

            if(void.class.equals(key.signature.returnType().getRawType())) {
                for(MethodHandle handle : methodHandles) {
                    handle.invokeWithArguments(flatArgs);
                }
                return Collections.emptyList();
            } else {
                final List<R> results = new ArrayList<>(methodHandles.size());
                for(MethodHandle handle : methodHandles) {
                    results.add((R) handle.invokeWithArguments(flatArgs));
                }
                return results;
            }
        }
    }

    private final LoadingCache<InvocationKey<?, ?>, InvocationHandle<?, ?>> invocations = CacheUtils.newCache(InvocationHandle::new);

    public <T, R> InvocationHandle<T, R> handle(InvocationKey<T, R> key) {
        return (InvocationHandle<T, R>) invocations.getUnchecked(key);
    }

    public <T, R> InvocationHandle<T, R> handle(TypeToken<T> target, String name, GenericMethodType<R> signature) {
        return handle(new InvocationKey<>(target, name, signature));
    }

    public <T, R> InvocationHandle<T, R> handle(TypeToken<T> target, String name, TypeToken<R> returnType, List<TypeToken<?>> argumentTypes) {
        return handle(target, name, new GenericMethodType<>(returnType, argumentTypes));
    }

    public <T, R> InvocationHandle<T, R> handle(Class<T> target, String name, Class<R> returnType, List<Class<?>> argumentTypes) {
        return handle(TypeToken.of(target), name, TypeToken.of(returnType), argumentTypes.stream().map(TypeToken::of).collect(Collectors.toList()));
    }

    public <T, R> List<R> invoke(TypeToken<T> targetType, String name, TypeToken<R> returnType, List<TypeToken<?>> argumentTypes, T target, List<Object> arguments) throws Throwable {
        return handle(targetType, name, returnType, argumentTypes).invoke(target, arguments);
    }
}
