package tc.oc.api.minecraft.sessions;

import tc.oc.api.sessions.SessionService;
import tc.oc.commons.core.inject.HybridManifest;

public class MinecraftSessionsManifest extends HybridManifest {

    @Override
    protected void configure() {
        bindAndExpose(LocalSessionFactory.class);

        publicBinder().forOptional(SessionService.class)
                      .setDefault().to(LocalSessionService.class);
    }
}
