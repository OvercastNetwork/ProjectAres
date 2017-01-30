package tc.oc.commons.core.server;

import tc.oc.commons.core.event.EventBusModule;
import tc.oc.commons.core.inject.InjectorScope;
import tc.oc.commons.core.inject.Manifest;
import tc.oc.commons.core.inject.UtilCoreManifest;
import tc.oc.commons.core.plugin.PluginScoped;

/**
 * Global platform-agnostic bindings. Singletons shared by all plugins go here.
 *
 * These bindings will be in the master injector. All plugins will have access to
 * these bindings, but these bindings cannot see anything in the local plugin modules.
 * So, things bound here cannot try to inject anything plugin-specific.
 *
 * Note: Nothing here is particularly Minecraft related, at the moment
 */
public class MinecraftServerManifest extends Manifest {
    @Override
    protected void configure() {
        install(new UtilCoreManifest());

        // Platform-neutral event bus. Can be used for cross-platform events.
        // Currently not integrated with the platform-specific event systems.
        install(new EventBusModule());


        // Enable the PluginScoped annotation
        bindScope(PluginScoped.class, new InjectorScope());
    }
}
