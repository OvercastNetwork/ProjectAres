package tc.oc.commons.core.plugin;

import tc.oc.commons.core.commands.CommandsManifest;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.minecraft.scheduler.MinecraftExecutorManifest;

/**
 * Per-plugin platform-agnostic bindings
 */
public class MinecraftPluginManifest extends HybridManifest {

    @Override
    protected void configure() {
        install(new CommandsManifest());
        install(new MinecraftExecutorManifest());
    }
}
