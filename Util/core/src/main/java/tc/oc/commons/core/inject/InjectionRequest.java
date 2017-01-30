package tc.oc.commons.core.inject;

import javax.inject.Inject;

import com.google.inject.MembersInjector;
import com.google.inject.TypeLiteral;
import tc.oc.commons.core.reflect.ResolvableType;

/**
 * Hacky replacement for broken {@link com.google.inject.Binder#requestInjection(TypeLiteral, Object)}
 */
public class InjectionRequest<T> extends Manifest {

    private final T instance;
    private final TypeLiteral<T> type;

    public InjectionRequest(T instance, TypeLiteral<T> type) {
        this.instance = instance;
        this.type = type;
    }

    protected InjectionRequest(T instance) {
        this.instance = instance;
        this.type = new ResolvableType<T>(){}.in(getClass());
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(instance);
    }

    @Override
    public boolean equals(Object that) {
        return this == that || (that instanceof InjectionRequest &&
                                this.instance == ((InjectionRequest) that).instance);
    }

    @Override
    protected void configure() {
        requestInjection(this);
        injector = getMembersInjector(type);
    }

    private MembersInjector<T> injector;

    @Inject private void inject() {
        injector.injectMembers(instance);
    }
}
