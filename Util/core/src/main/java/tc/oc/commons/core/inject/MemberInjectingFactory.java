package tc.oc.commons.core.inject;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;

import com.google.inject.MembersInjector;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.HasDependencies;
import com.google.inject.spi.InjectionPoint;
import tc.oc.commons.core.util.ExceptionUtils;

/**
 * A generic factory that creates {@link T} instances in the same way as an Injector,
 * but does not require any binding for {@link T} itself.
 *
 * The injectable constructor is used to instantiate the class, but none of the
 * parameters are injected, they must all be passed to {@link #newInstance(Object...)}.
 * However, fields and instance methods *are* injected, and dependencies for those
 * injection points are statically propagated to this class.
 */
public class MemberInjectingFactory<T> implements HasDependencies {

    protected final TypeLiteral<T> type;
    protected final InjectionPoint injectionPoint;
    protected final Set<Dependency<?>> dependencies = new HashSet<>();

    private final Constructor<T> constructor;
    private final MembersInjector<T> injector;

    @Inject public MemberInjectingFactory(TypeLiteral<T> type, MembersInjector<T> injector) {
        this.type = type;
        this.injector = injector;
        this.injectionPoint = InjectionPoint.forConstructorOf(type);
        this.constructor = (Constructor<T>) injectionPoint.getMember();
        this.constructor.setAccessible(true);

        dependencies.addAll(Dependency.forInjectionPoints(InjectionPoint.forInstanceMethodsAndFields(type)));
    }

    @Override
    public Set<Dependency<?>> getDependencies() {
        return dependencies;
    }

    public T newInstance(Object... args) {
        final T instance = ExceptionUtils.propagate(() -> constructor.newInstance(args));
        injector.injectMembers(instance);
        return instance;
    }
}
