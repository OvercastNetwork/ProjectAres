package tc.oc.api.document;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.cache.LoadingCache;
import tc.oc.api.docs.virtual.Document;
import tc.oc.api.docs.virtual.Model;
import tc.oc.api.exceptions.SerializationException;
import tc.oc.commons.core.inspect.Inspectable;
import tc.oc.commons.core.inspect.InspectableProperty;
import tc.oc.commons.core.reflect.MethodHandleUtils;
import tc.oc.commons.core.reflect.Methods;
import tc.oc.commons.core.stream.BiStream;
import tc.oc.commons.core.util.CacheUtils;

public class ProxyDocumentGenerator implements DocumentGenerator {

    @Override
    public <T extends Document> T instantiate(DocumentMeta<T> meta, Document base, Map<String, Object> data) {
        // Validate data before creating the document
        for(Map.Entry<String, Getter> entry : meta.getters().entrySet()) {
            final String name = entry.getKey();
            final Getter getter = entry.getValue();

            if(data.containsKey(name)) {
                getter.validate(data.get(name));
            } else if(!getter.hasDefault()) {
                throw new IllegalArgumentException("Missing value for required property " + name);
            }
        }

        return new Invoker<>(meta, base, data).proxy;
    }

    private static final Method Object_toString = Methods.declaredMethod(Object.class, "toString");
    private static final Method Inspectable_inspect = Methods.declaredMethod(Inspectable.class, "inspect");

    private class Invoker<T extends Document> implements InvocationHandler, Inspectable {

        final DocumentMeta<T> meta;
        final Map<String, Object> data;
        final LoadingCache<Method, MethodHandle> handles;
        final T proxy;

        Invoker(DocumentMeta<T> meta, Document base, Map<String, Object> data) {
            this.meta = meta;
            this.data = data;

            this.proxy = (T) Proxy.newProxyInstance(meta.type().getClassLoader(), new Class[]{meta.type(), Inspectable.class}, this);

            this.handles = CacheUtils.newCache(method -> {
                if(method.getDeclaringClass().isAssignableFrom(Inspectable.class) &&
                   !method.getDeclaringClass().isAssignableFrom(Object.class)) {
                    // Send Inspectable methods to this
                    return MethodHandles.lookup()
                                        .unreflect(method)
                                        .bindTo(this);
                } else if(method.equals(Object_toString)) {
                    // Send toString to this.inspect()
                    return MethodHandles.lookup()
                                        .unreflect(Inspectable_inspect)
                                        .bindTo(this);
                }

                final String name = method.getName();
                final Getter getter;

                // If method is a property getter, and we have a value for the property,
                // return a constant method handle that just returns the value.
                if(method.getParameterTypes().length == 0) {
                    getter = meta.getters().get(name);
                    if(getter != null && data.containsKey(name)) {
                        return MethodHandles.constant(method.getReturnType(), data.get(name));
                    }
                } else {
                    getter = null;
                }

                // If the base class implements the method, call that one.
                if(method.getDeclaringClass().isInstance(base)) {
                    return MethodHandles.lookup()
                                        .unreflect(method)
                                        .bindTo(base);
                }

                // If method has a default implementation in the document interface,
                // return a method handle that calls that implementation directly,
                // with the proxy as target object. We can't just invoke the method
                // the normal way, because the proxy would intercept it again.
                if(method.isDefault()) {
                    return MethodHandleUtils.defaultMethodHandle(method)
                                            .bindTo(proxy);
                }

                // If the method is a nullable property, we can return null as a default value.
                if(getter != null && getter.isNullable()) {
                    return MethodHandles.constant(method.getReturnType(), null);
                }

                throw new SerializationException("No implementation for method '" + name + "'");
            });
        }

        @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return handles.getUnchecked(method).invokeWithArguments(args);
        }

        @Override
        public String inspectType() {
            return meta.type().getSimpleName();
        }

        @Override
        public Optional<String> inspectIdentity() {
            if(proxy instanceof Model) {
                return Optional.of(((Model) proxy)._id());
            }
            return Optional.empty();
        }

        @Override
        public Stream<? extends InspectableProperty> inspectableProperties() {
            return BiStream.from(data)
                           .merge(InspectableProperty::of);
        }
    }
}
