package tc.oc.evil;

import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;
import tc.oc.commons.core.reflect.MethodResolver;
import tc.oc.commons.core.reflect.Methods;

/**
 * Generates classes that implement the decorator pattern, wrapping another object
 * of a common supertype, and forwarding some methods to that object, while overriding
 * others to alter its behavior.
 *
 * Specifically, this factory takes a possibly abstract class implementing {@link Decorator},
 * and generates a non-abstract subclass that forwards all methods to whatever is returned
 * from {@link Decorator#delegate()}. The decorated type can be an interface, an abstract class,
 * or a concrete class. Methods that are overridden in the decorator are not forwarded,
 * nor are methods that do not exist in the decorated class at all.
 *
 * The generated subclass will inherit all accessible constructors from its superclass.
 * However, these cannot be called directly. Instances must be created through the
 * create methods in this class.
 *
 * Example:
 * <pre>
 *     interface Thing {
 *         void woot();
 *         void donk();
 *     }
 *
 *     class BoringThing implements Thing {
 *         &#64;Override public void woot() { ... }
 *         &#64;Override public void donk() { ... }
 *     }
 *
 *     abstract class SuperThing implements Thing, Decorator&lt;Thing&gt; {

 *         private final Thing thing;
 *         protected SuperThing(Thing thing) { this.thing = thing; }
 *         &#64;Override public Thing delegate() { return thing; }

 *         &#64;Override public void woot() { ... }
 *     }
 *
 *     SuperThing st = decoratorFactory.create(
 *         Thing.class,
 *         SuperThing.class,
 *         new Class[]{ Thing.class },
 *         new Object[]{ new BoringThing() }
 *     );
 *
 *     st.woot() // calls SuperThing#woot()
 *     st.donk() // calls BoringThing#donk()
 *
 * </pre>
 */

public class DecoratorFactory {

    private static final DecoratorFactory INSTANCE = new DecoratorFactory(new LibCGDecoratorGenerator());
    public static DecoratorFactory get() {
        return INSTANCE;
    }

    private final DecoratorGenerator generator;
    private final Cache<Class<? extends Decorator<?>>, DecoratorGenerator.Meta<?, ?>> cache = CacheBuilder.newBuilder().build();

    private DecoratorFactory(DecoratorGenerator generator) {
        this.generator = generator;
    }

    public <T, D extends Decorator<T>> D create(Class<T> type, Class<D> decorator, Class<?>[] argumentTypes, Object[] arguments) {
        try {
            return meta(type, decorator).newInstance(argumentTypes, arguments);
        } catch(Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public <T, D extends Decorator<T>> D create(Class<T> type, Class<D> decorator) {
        try {
            return meta(type, decorator).newInstance();
        } catch(Exception e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Given a type, and another type that decorates it, return a non-abstract class
     * extending the decorator type, generating any needed delegating methods.
     *
     * Generated classes are cached and reused per decorator class.
     */
    public <T, D extends Decorator<T>> Class<? extends D> implement(Class<T> type, Class<D> decorator) {
        return meta(type, decorator).implementation;
    }

    private <T, D extends Decorator<T>> void validate(Class<T> type, Class<D> decorator) {
        final MethodResolver resolver = new MethodResolver(decorator);
        Methods.accessibleMethods(decorator).forEach(method -> {
            if(!(Methods.respondsTo(decorator, method) || resolver.hasMethod(type, method))) {
                throw new IllegalStateException("Method " + method +
                                                " is abstract in decorator " + decorator.getName() +
                                                " and cannot be forwarded to " + type.getName());
            }
        });
    }

    private <T, D extends Decorator<T>> DecoratorGenerator.Meta<T, D> meta(Class<T> type, Class<D> decorator) {
        // Try once without locking or creating the loader
        final DecoratorGenerator.Meta<?, ?> meta = cache.getIfPresent(decorator);
        if(meta != null) return (DecoratorGenerator.Meta<T, D>) meta;

        // Try again
        try {
            return (DecoratorGenerator.Meta<T, D>) cache.get(decorator, () -> {
                validate(type, decorator);
                return generator.implement(type, decorator);
            });
        } catch(ExecutionException | UncheckedExecutionException e) {
            throw (RuntimeException) e.getCause();
        }
    }
}
