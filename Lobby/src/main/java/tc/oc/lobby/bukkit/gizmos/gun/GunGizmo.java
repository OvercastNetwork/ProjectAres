package tc.oc.lobby.bukkit.gizmos.gun;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.bukkit.users.OnlinePlayers;
import tc.oc.api.bukkit.users.Users;
import tc.oc.commons.bukkit.raindrops.RaindropResult;
import tc.oc.commons.bukkit.raindrops.RaindropUtil;
import tc.oc.lobby.bukkit.Lobby;
import tc.oc.lobby.bukkit.LobbyTranslations;
import tc.oc.lobby.bukkit.gizmos.Gizmo;
import tc.oc.lobby.bukkit.gizmos.GizmoConfig;
import tc.oc.lobby.bukkit.gizmos.Gizmos;

public class GunGizmo extends Gizmo implements Listener {

    @Inject private static OnlinePlayers onlinePlayers;
    @Inject private static BukkitUserStore userStore;

    private final List<UUID> gifts = Lists.newArrayList();
    private final Map<Item, UUID> items = Maps.newHashMap();

    public GunGizmo(String name, String prefix, String description, Material icon, int cost) {
        super(name, prefix, description, icon, cost);
    }

    @Override
    public String getName(Player viewer) {
        return LobbyTranslations.get().t("gizmo.gun.name", viewer);
    }

    @Override
    public String getDescription(Player viewer) {
        return LobbyTranslations.get().t("gizmo.gun.description", viewer);
    }

    @Override
    protected void initialize() {
        Bukkit.getPluginManager().registerEvents(this, Lobby.get());
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Lobby.get(), new GunTask(), 0, GizmoConfig.TICK_UPDATE_DELAY);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Lobby.get(), new GunGiftTask(), 0, 2 * 20);
    }

    @EventHandler
    public void playerQuit(final PlayerQuitEvent event) {
        this.gifts.remove(event.getPlayer().getUniqueId());
        this.items.entrySet().removeIf(entry -> entry.getValue().equals(event.getPlayer().getUniqueId()));
    }

    @EventHandler
    public void playerInteract(final PlayerInteractEvent event) {
        if(event.getAction() == Action.PHYSICAL
                || !(Gizmos.gizmoMap.get(event.getPlayer()) instanceof GunGizmo)
                || event.getItem() == null || event.getItem().getType() != this.getIcon()) return;

        final Player player = event.getPlayer();
        RaindropUtil.giveRaindrops(Users.playerId(player), -1, new RaindropResult() {
            @Override
            public void run() {
                if(success) {
                    Vector velocity = player.getLocation().getDirection().multiply(1.75D);

                    Item item = player.getWorld().dropItem(event.getPlayer().getEyeLocation(), new ItemStack(Material.GHAST_TEAR));
                    item.setVelocity(velocity);
                    item.setTicksLived((5 * 60 * 20) - (5 * 20)); // 5 minutes - 5 seconds
                    items.put(item, player.getUniqueId());
                } else {
                    player.sendMessage(ChatColor.RED + LobbyTranslations.get().t("gizmo.gun.empty", player));
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                }
            }
        }, null, false, true, false);
    }

    private class GunGiftTask implements Runnable {
        @Override
        public void run() {
            try {
                final HashMap<UUID, Integer> counts = Maps.newHashMap();
                gifts.forEach(playerId -> counts.merge(playerId, 1, Integer::sum));

                counts.forEach((playerId, count) -> onlinePlayers.byUuid(playerId).ifPresent(player -> {
                    RaindropUtil.giveRaindrops(
                        userStore.playerId(player),
                        count,
                        null,
                        new TranslatableComponent("gizmo.gun.raindropsResult"),
                        false
                    );
                }));
            } finally {
                gifts.clear();
            }
        }
    }

    private class GunTask implements Runnable {
        @Override
        public void run() {
            for(Item item : Bukkit.getWorlds().get(0).getEntitiesByClass(Item.class)) {
                if(item.getItemStack().getType() != Material.GHAST_TEAR) continue;
                UUID skip = Gizmos.gunGizmo.items.get(item);

                for(Entity entity : item.getNearbyEntities(0.5d, 0.5d, 0.5d)) {
                    if(entity instanceof Player && !entity.getUniqueId().equals(skip)) {
                        Player player = (Player) entity;
                        if(player.hasPermission("gizmo.immunity")) continue;
                        player.damage(0d, item);
                        Gizmos.gunGizmo.gifts.add(player.getUniqueId());
                        item.remove();
                        break;
                    }
                }

                if(item.getTicksLived() >= 6000) item.remove();
            }
        }
    }
}
