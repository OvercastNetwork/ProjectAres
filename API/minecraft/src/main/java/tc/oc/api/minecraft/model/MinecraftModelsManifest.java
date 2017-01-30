package tc.oc.api.minecraft.model;

import java.util.concurrent.ExecutorService;

import com.google.inject.Key;
import tc.oc.api.model.ModelSync;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.plugin.PluginFacetBinder;
import tc.oc.minecraft.scheduler.Sync;

public class MinecraftModelsManifest extends HybridManifest {

    @Override
    protected void configure() {
        // We want a global binding for @ModelSync ExecutorService, but each plugin has
        // its own executors, so just use the API plugin's executor globally.
        bind(Key.get(ExecutorService.class, ModelSync.class))
            .to(Key.get(ExecutorService.class, Sync.immediate));

        final PluginFacetBinder facets = new PluginFacetBinder(binder());
        facets.register(ModelCommands.class);
        facets.register(ModelCommands.Parent.class);
    }
}
