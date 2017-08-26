package tc.oc.commons.bukkit.inject;

import java.util.UUID;
import javax.inject.Qualifier;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import tc.oc.minecraft.protocol.MinecraftVersion;

/**
 * Binds various services provided through a {@link Player} (but does not bind {@link Player} itself)
 *
 * TODO: Might be handy to bind things like Scoreboard, but that is not specific to players
 * so it might collide with other bindings e.g. if the server's main Scoreboard was bound to
 * the same key in an outer scope. Perhaps these bindings should have some {@link Qualifier},
 * but what would that be exactly?
 */
public class BukkitPlayerModule extends AbstractModule {
    @Override protected void configure() {}

    @Provides PlayerInventory inventory(Player player) {
        return player.getInventory();
    }

    @Provides UUID uuid(Player player) {
        return player.getUniqueId();
    }

    @Provides MinecraftVersion version(Player player) {
        return MinecraftVersion.byProtocol(player.getProtocolVersion());
    }
}
