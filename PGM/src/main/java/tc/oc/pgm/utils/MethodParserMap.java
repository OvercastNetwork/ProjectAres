package tc.oc.pgm.utils;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;

import com.google.api.client.util.Throwables;
import com.google.common.reflect.TypeToken;
import org.jdom2.Element;
import tc.oc.commons.core.reflect.MethodHandleUtils;
import tc.oc.commons.core.reflect.Types;
import tc.oc.commons.core.util.ExceptionUtils;
import tc.oc.commons.core.util.Optionals;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

public class MethodParserMap<T> {

    private class Record {
        final Object target;
        final Method method;
        final MethodHandle handle;
        final boolean passElement;
        final boolean optional;

        private Record(Object target, Method method) {
            this.target = target;
            this.method = method;

            final TypeToken<?> returnType;
            if(Optional.class.isAssignableFrom(method.getReturnType())) {
                optional = true;
                returnType = Optionals.elementType(method.getGenericReturnType());
            } else {
                optional = false;
                returnType = TypeToken.of(method.getGenericReturnType());
            }

            if(!type.isAssignableFrom(returnType)) {
                throw new IllegalStateException("Method " + method + " return type " + returnType + " is not assignable to " + type);
            }

            if(method.getParameterTypes().length == 0) {
                passElement = false;
            } else  {
                if(!(method.getParameterTypes().length == 1 && Element.class.isAssignableFrom(method.getParameterTypes()[0]))) {
                    throw new IllegalStateException("Method " + method + " should take no parameters, or a single Element parameter");
                }
                passElement = true;
            }

            try {
                this.handle = MethodHandleUtils.privateLookup(method.getDeclaringClass())
                                               .unreflect(method)
                                               .bindTo(target);
            } catch(IllegalAccessException e) {
                throw Throwables.propagate(e);
            }
        }

        Object invoke0(Element el) throws InvalidXMLException {
            try {
                if(passElement) {
                    return handle.invoke(el);
                } else {
                    return handle.invoke();
                }
            } catch(InvalidXMLException e) {
                e.offerNode(new Node(el));
                throw e;
            } catch(Throwable e) {
                throw ExceptionUtils.propagate(e);
            }
        }

        T invoke(Element el) throws InvalidXMLException {
            if(optional) {
                throw new IllegalStateException("Invoked optional method as required");
            }
            return (T) invoke0(el);
        }

        Optional<T> tryInvoke(Element el) throws InvalidXMLException {
            return optional ? (Optional<T>) invoke0(el)
                            : Optional.of((T) invoke0(el));
        }
    }

    private final TypeToken<T> type;
    private final Map<String, Record> methods = new HashMap<>();

    public MethodParserMap(@Nullable TypeToken<T> type) {
        this.type = type != null ? type : Types.assertFullySpecified(new TypeToken<T>(getClass()){});
    }

    public void register(String name, Object target, Method method) {
        final Record record = methods.get(name);
        if(record == null || record.method.getDeclaringClass().isAssignableFrom(method.getDeclaringClass())) {
            // Method is new, or overrides a superclass method
            methods.put(name, new Record(target, method));
        } else if(!method.getDeclaringClass().isAssignableFrom(record.method.getDeclaringClass())) {
            // Method is not equal to, or overridden by, the existing one
            throw new IllegalStateException("Conflicting parse method '" + name +
                                            "' declared in " + record.method.getDeclaringClass().getName() +
                                            " and " + method.getDeclaringClass().getName());
        }
    }

    public void register(Object target, Method method) {
        final MethodParser annot = method.getAnnotation(MethodParser.class);
        if(annot != null) {
            if(annot.value().length == 0) {
                register(method.getName().replace('_', '-'), target, method);
            } else {
                for(String name : annot.value()) {
                    register(name, target, method);
                }
            }
        }
    }

    public void register(Object target) {
        for(Class<?> cls : Types.ancestors(target.getClass())) {
            for(Method method : cls.getDeclaredMethods()) {
                register(target, method);
            }
        }
    }

    public boolean hasMethod(String name) {
        return methods.containsKey(name);
    }

    public boolean canParse(Element el) {
        return hasMethod(el.getName());
    }

    private Record record(Element el) throws InvalidXMLException {
        final Record record = methods.get(el.getName());
        if(record == null) {
            throw new InvalidXMLException("Unrecognized element", el);
        }
        return record;
    }

    public T parse(Element el) throws InvalidXMLException {
        return record(el).invoke(el);
    }

    public Optional<T> tryParse(Element el) throws InvalidXMLException {
        return record(el).tryInvoke(el);
    }
}
