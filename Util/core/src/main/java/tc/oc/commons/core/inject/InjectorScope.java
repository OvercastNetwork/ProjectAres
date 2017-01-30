package tc.oc.commons.core.inject;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.internal.SingletonScope;

/**
 * Completely obsolete, just use @Singleton
 */
@Deprecated
public class InjectorScope implements Scope {

    private final SingletonScope singletonScope = new SingletonScope();

    @Override
    public <T> Provider<T> scope(Key<T> key, Provider<T> unscoped) {
        return singletonScope.scope(key, unscoped);
    }
}
