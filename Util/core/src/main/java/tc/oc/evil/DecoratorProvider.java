package tc.oc.evil;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.inject.Injector;

import static com.google.common.base.Preconditions.checkState;

/**
 * A {@link Provider<D>} that creates {@link Decorator <D>} instances,
 * using an injected {@link DecoratorFactory}.
 *
 * The decorator class is always instantiated through its default constructor,
 * which must exist. This means that nothing can be injected through the
 * constructor. However, its members are injected after instantiation.
 *
 * TODO: Would be better if the decorator class was generated and bound
 * at configuration time, so errors would happen earlier, but we need
 * an injected factory before we can generate the class.
 */
public class DecoratorProvider<T, D extends Decorator<T>> implements Provider<D> {

    @Inject Injector injector;
    @Inject private DecoratorFactory factory;

    private final Class<T> originalType;
    private final Class<D> decoratorType;

    public DecoratorProvider(Class<T> originalType, Class<D> decoratorType) {
        this.originalType = originalType;
        this.decoratorType = decoratorType;
    }

    @Override
    public D get() {
        checkState(injector != null, DecoratorProvider.class.getSimpleName() +
                                     " has not yet had its own dependencies injected");

        final D d = factory.create(originalType, decoratorType);
        injector.injectMembers(d);
        return d;
    }
}
