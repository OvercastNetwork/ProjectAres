package tc.oc.commons.core.inject;

import java.lang.annotation.Annotation;
import java.util.Optional;

import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.OutOfScopeException;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.Scope;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import tc.oc.commons.core.reflect.ResolvableType;
import tc.oc.commons.core.util.Threadable;
import tc.oc.commons.core.util.ThrowingRunnable;
import tc.oc.commons.core.util.ThrowingSupplier;

/**
 * A {@link Scope} that stores and retrieves objects from a thread-local {@link InjectionStore}.
 */
public abstract class InjectionScope<A extends Annotation> implements Scope {

    private final Class<A> annotation = (Class<A>) new ResolvableType<A>(){}.in(getClass()).getRawType();
    private final Key<InjectionScope<A>> scopeKey = Key.get(new ResolvableType<InjectionScope<A>>(){}.in(getClass()));
    private final Key<InjectionStore<A>> storeKey = Key.get(new ResolvableType<InjectionStore<A>>(){}.in(getClass()));
    private final Threadable<InjectionStore<A>> current = new Threadable<>();

    public Key<InjectionScope<A>> scopeKey() {
        return scopeKey;
    }

    public Key<InjectionStore<A>> storeKey() {
        return storeKey;
    }

    public <T> void bindSeeded(Binder binder, Key<T> key) {
        binder.bind(key).toProvider(() -> {
            throw new ProvisionException("Missing seed instance for " + key);
        }).in(annotation);
    }

    public boolean isScoped(Binding<?> binding) {
        return Scopes.isScoped(binding, this, annotation);
    }

    public <E extends Throwable> void withCurrentStore(InjectionStore<A> store, ThrowingRunnable<E> block) throws E {
        current.let(store, block);
    }

    public <T, E extends Throwable> T withCurrentStore(InjectionStore<A> store, ThrowingSupplier<T, E> block) throws E {
        return current.let(store, block);
    }

    /**
     * Return the current store for the given key on the current thread, or null if there
     * is no store in scope.
     *
     * The base method returns the current thread-local store and ignores the key. Subclasses
     * can override this to provide other methods of resolving the scope.
     */
    protected Optional<InjectionStore<A>> currentStore(Key<?> key) {
        return Optional.ofNullable(current.get());
    }

    public <T> Optional<T> currentInstance(Key<T> key) {
        return currentStore(key).map(store -> store.provide(key));
    }

    public <T> Optional<T> currentInstance(TypeLiteral<T> type) { return currentInstance(Key.get(type)); }
    public <T> Optional<T> currentInstance(Class<T> type) { return currentInstance(Key.get(type)); }

    @Override
    public <T> Provider<T> scope(Key<T> key, Provider<T> unscoped) {
        return () -> {
            final InjectionStore<A> store = currentStore(key).orElseThrow(
                () -> new OutOfScopeException("Cannot provide " + key + " outside of " + annotation.getSimpleName() + " scope")
            );

            // If the current store already contains the key, return its value.
            // Otherwise, provision an unscoped value and add it to the store.
            // If the key is of the store itself, just use the store we have.
            return store.provide(key, () -> storeKey.equals(key) ? (T) store : unscoped.get());
        };
    }
}
