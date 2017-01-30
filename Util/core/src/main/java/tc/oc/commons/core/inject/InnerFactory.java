package tc.oc.commons.core.inject;

/**
 * A factory that creates instances of {@link I} from given instances of {@link O}.
 *
 * This factory can be generated and bound automatically by installing an {@link InnerFactoryManifest}.
 * In that case, {@link I} is expected to have an injectable constructor with first parameter {@link O}.
 * This will be implicitly true if {@link I} is an inner class of {@link O}, but that is not a requirement.
 * Note that if {@link O} is an inner class, it ALWAYS needs an explicit constructor with an @Inject
 * annotation, since the constructor always takes at least one parameter.
 *
 * The generated factory will perform all standard injections on the {@link I} instances it creates,
 * except for the {@link O} parameter, which is passed to {@link #create(O)}.
 *
 * This is similar to assisted-inject, with the important difference that it supports inner classes.
 * It also does not require a factory interface to be defined for every binding.
 *
 * Example:
 *
 *     class Outer {
 *         @Inject InnerFactory<Outer, Inner> factory;
 *
 *         Inner newInner() {
 *             return factory.create(this);
 *         }
 *
 *         class Inner {
 *             @Inject Inner(SomeDependency x) { ... }
 *         }
 *     }
 */
public interface InnerFactory<O, I> {
    I create(O outer);
}
