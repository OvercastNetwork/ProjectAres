package tc.oc.api.minecraft.maps;

import tc.oc.api.maps.MapService;
import tc.oc.commons.core.inject.HybridManifest;

public class MinecraftMapsManifest extends HybridManifest {

    @Override
    protected void configure() {
        publicBinder().forOptional(MapService.class)
                      .setDefault().to(LocalMapService.class);
    }
}
