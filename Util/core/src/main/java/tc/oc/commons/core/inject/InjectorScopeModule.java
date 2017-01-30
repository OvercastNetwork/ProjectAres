package tc.oc.commons.core.inject;

import com.google.inject.AbstractModule;

public class InjectorScopeModule extends AbstractModule {
    @Override
    protected void configure() {
        bindScope(InjectorScoped.class, new InjectorScope());
    }
}
