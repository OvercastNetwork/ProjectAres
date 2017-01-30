package tc.oc.commons.core.plugin;

import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.minecraft.api.event.ListenerBinder;

public class PluginFacetManifest extends HybridManifest {
    @Override
    protected void configure() {
        new PluginFacetBinder(binder());
        new ListenerBinder(binder())
            .bindListener().to(PluginFacetLoader.class);
    }
}
