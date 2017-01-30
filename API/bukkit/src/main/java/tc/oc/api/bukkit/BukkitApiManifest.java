package tc.oc.api.bukkit;

import java.util.Optional;

import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import tc.oc.api.bukkit.friends.OnlineFriends;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.bukkit.users.Users;
import tc.oc.api.ApiManifest;
import tc.oc.api.minecraft.MinecraftApiManifest;
import tc.oc.api.minecraft.users.UserStore;
import tc.oc.bukkit.logging.RavenPlugin;
import tc.oc.commons.bukkit.inject.BukkitPluginManifest;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.inject.Manifest;
import tc.oc.commons.core.plugin.PluginResolver;
import tc.oc.minecraft.logging.BetterRaven;

public final class BukkitApiManifest extends HybridManifest {

    private static class Public extends Manifest {
        @Provides
        Optional<BetterRaven> betterRaven(PluginResolver<Plugin> resolver) {
            return Optional.ofNullable(resolver.getPlugin(RavenPlugin.class)).map(RavenPlugin::getRaven);
        }
    }

    @Override
    protected void configure() {
        install(new ApiManifest());
        install(new MinecraftApiManifest());
        install(new BukkitPluginManifest());

        publicBinder().install(new Public());

        bindAndExpose(UserStore.class).to(BukkitUserStore.class);
        bindAndExpose(BukkitUserStore.class);

        bindAndExpose(tc.oc.api.minecraft.users.OnlinePlayers.class).to(tc.oc.api.bukkit.users.OnlinePlayers.class);
        bindAndExpose(new TypeLiteral<tc.oc.api.minecraft.users.OnlinePlayers<Player>>(){}).to(tc.oc.api.bukkit.users.OnlinePlayers.class);
        bindAndExpose(tc.oc.api.bukkit.users.OnlinePlayers.class).to(BukkitUserStore.class);
        bindAndExpose(OnlineFriends.class).to(BukkitUserStore.class);

        requestStaticInjection(Users.class);
    }
}
