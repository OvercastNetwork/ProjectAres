package tc.oc.pgm.freeze;

import java.util.Collections;
import javax.inject.Inject;

import com.sk89q.minecraft.util.commands.CommandException;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import tc.oc.commons.bukkit.localization.Translations;
import tc.oc.commons.core.commands.CommandExceptionHandler;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.pgm.events.ObserverInteractEvent;
import tc.oc.commons.bukkit.event.ObserverKitApplyEvent;

public class FreezeListener implements Listener, PluginFacet {

    private final Freeze freeze;
    private final CommandExceptionHandler.Factory exceptionHandlerFactory;

    @Inject FreezeListener(Freeze freeze, CommandExceptionHandler.Factory exceptionHandlerFactory) {
        this.freeze = freeze;
        this.exceptionHandlerFactory = exceptionHandlerFactory;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteractEntity(final ObserverInteractEvent event) {
        if(event.getPlayer().isDead()) return;

        if(freeze.isFrozen(event.getPlayer().getBukkit())) {
            event.setCancelled(true);

        } else if(freeze.enabled()) {
            if(event.getClickedItem() != null &&
               event.getClickedItem().getType() == Material.ICE &&
               event.getPlayer().getBukkit().hasPermission(Freeze.PERMISSION) &&
               event.getClickedPlayer() != null) {
                event.setCancelled(true);

                try {
                    freeze.toggleFrozen(event.getPlayer().getBukkit(), event.getClickedPlayer().getBukkit());
                } catch(CommandException e) {
                    exceptionHandlerFactory
                        .create(event.getPlayer().getBukkit())
                        .handleException(e, null, null);
                }
            }
        }
    }

    @EventHandler
    public void giveKit(final ObserverKitApplyEvent event) {
        if(event.getPlayer().hasPermission(Freeze.PERMISSION)) {
            ItemStack item = new ItemStack(Material.ICE);
            ItemMeta meta = item.getItemMeta();
            meta.addItemFlags(ItemFlag.values());
            meta.setDisplayName(Translations.get().t("freeze.itemName", event.getPlayer()));
            meta.setLore(Collections.singletonList(Translations.get().t("freeze.itemDescription", event.getPlayer())));
            item.setItemMeta(meta);

            event.getPlayer().getInventory().setItem(6, item);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerMove(final PlayerMoveEvent event) {
        if(freeze.isFrozen(event.getPlayer())) {
            Location old = event.getFrom();
            old.setPitch(event.getTo().getPitch());
            old.setYaw(event.getTo().getYaw());
            event.setTo(old);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onVehicleMove(final VehicleMoveEvent event) {
        if(!event.getVehicle().isEmpty() && freeze.isFrozen(event.getVehicle().getPassenger())) {
            event.getVehicle().setVelocity(new Vector(0, 0, 0));
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onVehicleEnter(final VehicleEnterEvent event) {
        if(freeze.isFrozen(event.getEntered())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onVehicleExit(final VehicleExitEvent event) {
        if(freeze.isFrozen(event.getExited())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent event) {
        if(freeze.isFrozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockPlace(final BlockPlaceEvent event) {
        if(freeze.isFrozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBucketFill(final PlayerBucketFillEvent event) {
        if(freeze.isFrozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBucketEmpty(final PlayerBucketEmptyEvent event) {
        if(freeze.isFrozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW) // ignoreCancelled doesn't seem to work well here
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if(freeze.isFrozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInventoryClick(final InventoryClickEvent event) {
        if(event.getWhoClicked() instanceof Player) {
            if(freeze.isFrozen(event.getWhoClicked())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerDropItem(final PlayerDropItemEvent event) {
        if(freeze.isFrozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityDamge(final EntityDamageByEntityEvent event) {
        if(freeze.isFrozen(event.getDamager())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onVehicleDamage(final VehicleDamageEvent event) {
        if(freeze.isFrozen(event.getAttacker())) {
            event.setCancelled(true);
        }
    }
}
