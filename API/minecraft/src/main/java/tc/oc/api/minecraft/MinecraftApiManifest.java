package tc.oc.api.minecraft;

import com.google.inject.Provides;
import tc.oc.api.config.ApiConfiguration;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.minecraft.config.MinecraftApiConfiguration;
import tc.oc.api.minecraft.config.MinecraftApiConfigurationImpl;
import tc.oc.api.minecraft.connectable.ConnectablesManifest;
import tc.oc.api.minecraft.logging.MinecraftLoggingManifest;
import tc.oc.api.minecraft.maps.MinecraftMapsManifest;
import tc.oc.api.minecraft.model.MinecraftModelsManifest;
import tc.oc.api.minecraft.servers.MinecraftServersManifest;
import tc.oc.api.minecraft.sessions.MinecraftSessionsManifest;
import tc.oc.api.minecraft.users.MinecraftUsersManifest;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.inject.Manifest;
import tc.oc.debug.LeakDetectorManifest;
import tc.oc.minecraft.logging.RavenApiModule;

public final class MinecraftApiManifest extends HybridManifest {

    private static class Public extends Manifest {
        @Override
        protected void configure() {
            bind(ServerDoc.Identity.class).to(Server.class);
        }

        @Provides Server localServer(MinecraftService minecraftService) {
            return minecraftService.everfreshLocalServer();
        }
    }

    @Override
    protected void configure() {
        publicBinder().install(new Public());

        install(new RavenApiModule());

        install(new LeakDetectorManifest());
        install(new MinecraftLoggingManifest());
        install(new ConnectablesManifest());
        install(new MinecraftModelsManifest());

        install(new MinecraftServersManifest());
        install(new MinecraftUsersManifest());
        install(new MinecraftSessionsManifest());
        install(new MinecraftMapsManifest());

        bindAndExpose(ApiConfiguration.class).to(MinecraftApiConfiguration.class);
        bindAndExpose(MinecraftApiConfiguration.class).to(MinecraftApiConfigurationImpl.class);

        bindAndExpose(MinecraftService.class).to(MinecraftServiceImpl.class);
        bind(MinecraftServiceImpl.class).asEagerSingleton();
    }
}
