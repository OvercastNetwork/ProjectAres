package tc.oc.commons.core.inject;

import java.util.stream.Stream;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.InjectionPoint;

public final class Dependencies {
    private Dependencies() {}

    public static Stream<Dependency<?>> forInstanceMethodsAndFields(Class<?> type) {
        return forInstanceMethodsAndFields(TypeLiteral.get(type));
    }

    public static Stream<Dependency<?>> forInstanceMethodsAndFields(TypeLiteral<?> type) {
        return InjectionPoint.forInstanceMethodsAndFields(type)
                             .stream()
                             .flatMap(point -> point.getDependencies().stream());
    }
}
