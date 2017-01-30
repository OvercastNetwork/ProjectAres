package tc.oc.commons.bukkit.teleport;

import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.inject.InnerFactoryManifest;
import tc.oc.commons.core.plugin.PluginFacetBinder;

public class NavigatorManifest extends HybridManifest {
    @Override
    protected void configure() {
        install(InnerFactoryManifest.forInnerClass(NavigatorInterface.Configuration.class));

        expose(Navigator.class);
        expose(NavigatorInterface.class); // Only exposed so that other plugins can call setOpenButtonSlot

        final PluginFacetBinder facets = new PluginFacetBinder(binder());
        facets.register(Navigator.class);
        facets.register(NavigatorInterface.class);
    }
}
