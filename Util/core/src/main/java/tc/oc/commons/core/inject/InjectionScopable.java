package tc.oc.commons.core.inject;

import java.lang.annotation.Annotation;

import tc.oc.commons.core.util.ThrowingRunnable;
import tc.oc.commons.core.util.ThrowingSupplier;

/**
 * Useful methods for the "key" objects of {@link InjectionScope}s
 */
public interface InjectionScopable<A extends Annotation> {

    InjectionStore<A> injectionStore();

    InjectionScope<A> injectionScope();

    default <E extends Throwable> void asCurrentScope(ThrowingRunnable<E> block) throws E {
        injectionScope().withCurrentStore(injectionStore(), block);
    }

    default <R, E extends Throwable> R asCurrentScope(ThrowingSupplier<R, E> block) throws E {
        return injectionScope().withCurrentStore(injectionStore(), block);
    }
}
