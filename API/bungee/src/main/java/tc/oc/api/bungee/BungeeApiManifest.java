package tc.oc.api.bungee;

import java.util.Optional;

import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import tc.oc.api.ApiManifest;
import tc.oc.api.bungee.users.BungeeUserStore;
import tc.oc.api.minecraft.MinecraftApiManifest;
import tc.oc.api.minecraft.users.UserStore;
import tc.oc.bungee.logging.RavenPlugin;
import tc.oc.commons.bungee.inject.BungeePluginManifest;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.inject.Manifest;
import tc.oc.commons.core.plugin.PluginResolver;
import tc.oc.minecraft.logging.BetterRaven;

public final class BungeeApiManifest extends HybridManifest {

    public static class Public extends Manifest {
        @Provides
        Optional<BetterRaven> betterRaven(PluginResolver<Plugin> resolver) {
            return Optional.ofNullable(resolver.getPlugin(RavenPlugin.class)).map(RavenPlugin::getRaven);
        }
    }

    @Override
    protected void configure() {
        install(new ApiManifest());
        install(new MinecraftApiManifest());
        install(new BungeePluginManifest());

        publicBinder().install(new Public());

        bindAndExpose(UserStore.class).to(BungeeUserStore.class);
        bindAndExpose(BungeeUserStore.class);

        bindAndExpose(tc.oc.api.minecraft.users.OnlinePlayers.class).to(tc.oc.api.bungee.users.OnlinePlayers.class);
        bindAndExpose(new TypeLiteral<tc.oc.api.minecraft.users.OnlinePlayers<ProxiedPlayer>>(){}).to(tc.oc.api.bungee.users.OnlinePlayers.class);
        bindAndExpose(tc.oc.api.bungee.users.OnlinePlayers.class).to(BungeeUserStore.class);
    }
}
