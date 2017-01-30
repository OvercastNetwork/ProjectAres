package tc.oc.commons.core.inject;

import java.util.List;

import com.google.inject.Binding;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.DependencyAndSource;
import com.google.inject.spi.InjectionPoint;
import com.google.inject.spi.ProvisionListener;

/**
 * A {@link Provider} that knows the binding and injection point for each provision.
 * In other words, it knows what it is providing *to*, more or less.
 *
 * In order for this to work, the {@link ContextualProviderModule} must be installed.
 * That module installs a {@link ProvisionListener} which passes the details of each
 * provisioning to this class via a {@link ThreadLocal}. This is pretty hacky, but
 * Guice does not provide any straightforward way to do it.
 *
 * If this is a violation of some fundamental principle, I haven't been able to figure
 * out what that is. So, until the sky falls, we'll keep doing this, because it is
 * very, very useful.
 */
public abstract class ContextualProvider<T> implements Provider<T> {

    static final ThreadLocal<ProvisionListener.ProvisionInvocation<?>> provisionInvocation = new ThreadLocal<>();

    /**
     * Provides an instance of {@link T} for a given binding and dependency.
     * The {@link InjectionPoint}s are available through the {@link DependencyAndSource}
     * objects. The last dependency in the list is the one being directly provided.
     *
     * Note carefully the cases in which {@link DependencyAndSource#getDependency} and
     * {@link Dependency#getInjectionPoint} can return null.
     */
    protected abstract T getFor(Binding<T> binding, List<DependencyAndSource> dependencyChain);

    /**
     * Provided an instance of {@link T} when no provisioning context is available
     * i.e. when the {@link ProvisionListener} is not called. I don't know if or when
     * it is possible for this to happen.
     */
    protected T getWithoutContext() {
        throw new ProvisionException("No context available");
    }

    @Override
    final public T get() {
        final ProvisionListener.ProvisionInvocation<T> pi = (ProvisionListener.ProvisionInvocation<T>) provisionInvocation.get();
        if(pi == null) {
            return getWithoutContext();
        } else {
            return getFor(pi.getBinding(), pi.getDependencyChain());
        }
    }
}
