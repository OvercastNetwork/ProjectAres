package tc.oc.api.minecraft.servers;

import tc.oc.api.servers.ServerService;
import tc.oc.commons.core.inject.HybridManifest;

public class MinecraftServersManifest extends HybridManifest {
    @Override
    protected void configure() {
        bindAndExpose(StartupServerDocument.class);
        bindAndExpose(LocalServerDocument.class);

        publicBinder().forOptional(ServerService.class)
                      .setDefault().to(LocalServerService.class);
    }
}
