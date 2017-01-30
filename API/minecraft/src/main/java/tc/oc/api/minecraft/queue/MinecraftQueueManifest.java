package tc.oc.api.minecraft.queue;

import tc.oc.api.queue.QueueManifest;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.plugin.PluginFacetBinder;

public class MinecraftQueueManifest extends HybridManifest {

    @Override
    protected void configure() {
        install(new QueueManifest());

        new PluginFacetBinder(binder())
            .register(QueueCommands.Parent.class);
    }
}
