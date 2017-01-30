package tc.oc.commons.core.inject;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.google.inject.spi.InjectionPoint;
import tc.oc.commons.core.reflect.MethodFormException;

public class InjectionChecks {

    public static void checkInjectableCGLibProxyBase(Class<?> cls) {
        InjectionPoint.forInstanceMethodsAndFields(cls).forEach(ip -> {
            if(ip.getMember() instanceof Method && !Modifier.isPrivate(ip.getMember().getModifiers())) {
                // CGLib proxies override all non-private methods on the base class,
                // and do not copy method annotations, so Guice will not find the
                // @Inject annotation on the base method. Declaring the method
                // private works around this. The proxy will not try to override
                // private methods, and Guice can find them just fine.
                throw new MethodFormException(
                    (Method) ip.getMember(),
                    "Injected method on CGLib proxied class must be private (see exception site for details)"
                );
            }
        });
    }
}
