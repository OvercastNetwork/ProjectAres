package tc.oc.api.ocn;

import tc.oc.api.minecraft.queue.MinecraftQueueManifest;
import tc.oc.api.model.ModelBinders;
import tc.oc.commons.core.inject.HybridManifest;

public class OCNApiManifest extends HybridManifest implements ModelBinders {

    @Override
    protected void configure() {
        install(new OCNModelsManifest());
        install(new MinecraftQueueManifest());
    }
}
