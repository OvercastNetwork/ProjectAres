package tc.oc.api.bungee;

import com.google.inject.TypeLiteral;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import tc.oc.api.ApiManifest;
import tc.oc.api.bungee.users.BungeeUserStore;
import tc.oc.api.minecraft.MinecraftApiManifest;
import tc.oc.api.minecraft.users.UserStore;
import tc.oc.commons.bungee.inject.BungeePluginManifest;
import tc.oc.commons.core.inject.HybridManifest;

public final class BungeeApiManifest extends HybridManifest {

    @Override
    protected void configure() {
        install(new ApiManifest());
        install(new MinecraftApiManifest());
        install(new BungeePluginManifest());

        bindAndExpose(UserStore.class).to(BungeeUserStore.class);
        bindAndExpose(BungeeUserStore.class);

        bindAndExpose(tc.oc.api.minecraft.users.OnlinePlayers.class).to(tc.oc.api.bungee.users.OnlinePlayers.class);
        bindAndExpose(new TypeLiteral<tc.oc.api.minecraft.users.OnlinePlayers<ProxiedPlayer>>(){}).to(tc.oc.api.bungee.users.OnlinePlayers.class);
        bindAndExpose(tc.oc.api.bungee.users.OnlinePlayers.class).to(BungeeUserStore.class);
    }
}
