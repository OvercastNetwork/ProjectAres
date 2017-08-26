package tc.oc.commons.bukkit.freeze;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import tc.oc.commons.bukkit.event.CoarsePlayerMoveEvent;
import tc.oc.commons.bukkit.util.NMSHacks;
import tc.oc.commons.core.collection.WeakHashSet;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.commons.core.util.Pair;
import tc.oc.minecraft.api.scheduler.Tickable;

import static tc.oc.minecraft.protocol.MinecraftVersion.lessThan;
import static tc.oc.minecraft.protocol.MinecraftVersion.MINECRAFT_1_8;

/**
 * Freezes players by mounting them on an invisible minecart.
 */
@Singleton
public class PlayerFreezer implements PluginFacet, Listener, Tickable {

    private final Map<World, NMSHacks.FakeArmorStand> armorStands = new WeakHashMap<>();
    private final SetMultimap<Player, FrozenPlayer> frozenPlayers = HashMultimap.create();
    private final Map<Player, Pair<Boolean, Boolean>> legacyFrozenPlayers = new WeakHashMap<>();

    @Inject PlayerFreezer() {}

    @Override
    public Duration tickPeriod() {
        return Duration.ofMillis(50);
    }

    private NMSHacks.FakeArmorStand armorStand(Player player) {
        return armorStands.computeIfAbsent(player.getWorld(), NMSHacks.FakeArmorStand::new);
    }

    public boolean isFrozen(Player player) {
        return frozenPlayers.containsKey(player);
    }

    public FrozenPlayer freeze(Player player) {
        final FrozenPlayerImpl frozenPlayer = new FrozenPlayerImpl(player);
        final boolean wasFrozen = isFrozen(player);
        frozenPlayers.put(player, frozenPlayer);

        if(!wasFrozen) {
            player.setPaused(true);
            player.leaveVehicle(); // TODO: Put them back in the vehicle when thawed?
            armorStand(player).spawn(player, player.getLocation());
            sendAttach(player);
            if(lessThan(MINECRAFT_1_8, player.getProtocolVersion())) {
                boolean canFly = player.getAllowFlight(), isFlying = player.isFlying();
                legacyFrozenPlayers.put(player, Pair.create(canFly, isFlying));
                if(!player.isOnGround()) {
                    player.setAllowFlight(true);
                    player.setFlying(true);
                }
            }
        }

        return frozenPlayer;
    }

    @Override
    public void tick() {
        // If the player right-clicks on another vehicle while frozen, the client will
        // eject them from the freeze entity unconditionally, so we have to spam them
        // with these packets to keep them on it.
        frozenPlayers.keySet().forEach(this::sendAttach);
    }

    private void sendAttach(Player player) {
        armorStand(player).ride(player, player);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMove(CoarsePlayerMoveEvent event) {
        if(isFrozen(event.getPlayer()) && legacyFrozenPlayers.containsKey(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        frozenPlayers.removeAll(event.getPlayer());
        legacyFrozenPlayers.remove(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onUnload(WorldUnloadEvent event) {
        armorStands.remove(event.getWorld());
    }

    private class FrozenPlayerImpl implements FrozenPlayer {
        // Might eventually put some state here that can be restored after thawing,
        // e.g. gamemode, vehicle, etc. But currently, this class doesn't know enough
        // about the player's situation to do that safely.

        private final Player player;

        private FrozenPlayerImpl(Player player) {
            this.player = player;
        }

        @Override
        public void thaw() {
            if(frozenPlayers.remove(player, this) && !isFrozen(player) && player.isOnline()) {
                armorStand(player).destroy(player);
                player.setPaused(false);
                Pair<Boolean, Boolean> fly = legacyFrozenPlayers.remove(player);
                if(fly != null) {
                    player.setFlying(fly.second);
                    player.setAllowFlight(fly.first);
                }
            }
        }
    }
}
