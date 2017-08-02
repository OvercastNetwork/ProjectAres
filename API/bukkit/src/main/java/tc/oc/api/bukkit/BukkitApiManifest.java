package tc.oc.api.bukkit;

import tc.oc.api.bukkit.friends.OnlineFriends;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.bukkit.users.Users;

public final class BukkitApiManifest extends HybridManifest {

    @Override
    protected void configure() {
        install(new ApiManifest());
        install(new MinecraftApiManifest());
        install(new BukkitPluginManifest());

        bindAndExpose(UserStore.class).to(BukkitUserStore.class);
        bindAndExpose(BukkitUserStore.class);

        bindAndExpose(tc.oc.api.minecraft.users.OnlinePlayers.class).to(tc.oc.api.bukkit.users.OnlinePlayers.class);
        bindAndExpose(new TypeLiteral<tc.oc.api.minecraft.users.OnlinePlayers<Player>>(){}).to(tc.oc.api.bukkit.users.OnlinePlayers.class);
        bindAndExpose(tc.oc.api.bukkit.users.OnlinePlayers.class).to(BukkitUserStore.class);
        bindAndExpose(OnlineFriends.class).to(BukkitUserStore.class);

        requestStaticInjection(Users.class);
    }
}
