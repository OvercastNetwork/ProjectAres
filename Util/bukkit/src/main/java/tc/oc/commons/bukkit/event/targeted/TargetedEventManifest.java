package tc.oc.commons.bukkit.event.targeted;

import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.plugin.PluginFacetBinder;

public class TargetedEventManifest extends HybridManifest {

    @Override
    protected void configure() {
        bindAndExpose(TargetedEventBus.class).to(TargetedEventBusImpl.class);
        new TargetedEventRouterBinder(publicBinder());
        new PluginFacetBinder(binder())
            .register(TargetedEventBusImpl.class);
    }
}
