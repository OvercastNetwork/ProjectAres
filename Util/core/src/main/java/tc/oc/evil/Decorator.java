package tc.oc.evil;

/**
 * An object that may forward some of its method calls to another object of type {@link T},
 * which this object returns from its implementation of {@link #delegate()}.
 *
 * Generally, this object is also a subtype of {@link T}, thus implementing the decorator
 * design pattern, but nothing about this interface alone enforces that.
 *
 * This interface is intended to be used with {@link DecoratorFactory}, which can generate
 * classes that implement all the forwarding.
 */
public interface Decorator<T> {
    T delegate();
}

