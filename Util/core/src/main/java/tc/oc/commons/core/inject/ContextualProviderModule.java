package tc.oc.commons.core.inject;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.ProvisionListener;

/**
 * Installs necessary stuff to make {@link ContextualProvider}s work.
 */
public class ContextualProviderModule extends AbstractModule {
    @Override
    protected void configure() {
        bindListener(Matchers.any(), new ProvisionListener() {
            @Override
            public <T> void onProvision(ProvisionInvocation<T> provision) {
                final ProvisionListener.ProvisionInvocation<?> prior = ContextualProvider.provisionInvocation.get();
                ContextualProvider.provisionInvocation.set(provision);
                try {
                    provision.provision();
                } finally {
                    if(prior != null) {
                        ContextualProvider.provisionInvocation.set(prior);
                    } else {
                        ContextualProvider.provisionInvocation.remove();
                    }
                }
            }
        });
    }
}
