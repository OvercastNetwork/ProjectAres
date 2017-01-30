package tc.oc.pgm.modules;

import java.util.Iterator;
import javax.annotation.Nullable;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerAttackEntityEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.PortalCreateEvent;
import tc.oc.commons.bukkit.event.AdventureModeInteractEvent;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.ObserverInteractEvent;
import tc.oc.pgm.events.PlayerBlockTransformEvent;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchPlayerState;
import tc.oc.pgm.match.MatchScope;

/**
 * Listens to many events at low priority and cancels them if the actor is
 * not allowed to interact with the world. Also cancels a few events that
 * we just don't want ever.
 *
 * Any functionality beyond that should be implemented in other modules.
 * This module should be kept simple.
 */
@ListenerScope(MatchScope.LOADED)
public class EventFilterMatchModule extends MatchModule implements Listener {

    boolean cancel(Cancellable event, @Nullable MatchPlayer actor, @Nullable BaseComponent message) {
        logger.fine("Cancel " + event + " actor=" + actor);
        event.setCancelled(true);
        if(actor != null && message != null) {
            actor.sendWarning(message, true);
        }
        return true;
    }

    boolean cancel(Cancellable event, boolean cancel, World world, @Nullable MatchPlayer actor, @Nullable BaseComponent message) {
        if(cancel && getMatch().getWorld().equals(world)) {
            return cancel(event, actor, message);
        } else {
            logger.fine("Allow  " + event + " actor=" + actor);
            return false;
        }
    }

    boolean cancel(Cancellable event, boolean cancel, World world) {
        return cancel(event, cancel, world, null, null);
    }

    boolean cancelAlways(Cancellable event, World world) {
        return cancel(event, true, world);
    }

    boolean cancelUnlessInteracting(Cancellable event, MatchPlayer player) {
        return cancel(event, !player.canInteract(), player.getBukkit().getWorld(), player, null);
    }

    boolean cancelUnlessInteracting(Cancellable event, Entity entity) {
        return entity != null && cancel(event, !getMatch().canInteract(entity), entity.getWorld(), getMatch().getPlayer(entity), null);
    }

    boolean cancelUnlessInteracting(Cancellable event, MatchPlayerState player) {
        return cancel(event, !getMatch().canInteract(player), player.getMatch().getWorld(), null, null);
    }

    ClickType convertClick(ClickType clickType, Player player) {
        if(clickType == ClickType.RIGHT && player.isSneaking()) {
            return ClickType.SHIFT_RIGHT;
        } else {
            return clickType;
        }
    }

    @Nullable ClickType convertClick(Action action, Player player) {
        switch(action) {
            case LEFT_CLICK_BLOCK:
            case LEFT_CLICK_AIR:
                return ClickType.LEFT;

            case RIGHT_CLICK_BLOCK:
            case RIGHT_CLICK_AIR:
                return convertClick(ClickType.RIGHT, player);

            default:
                return null;
        }
    }

    // -------------------------------------------------------------
    // -- Unconditionally cancelled events i.e. rejected features --
    // -------------------------------------------------------------

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPortalCreate(final PortalCreateEvent event) {
        cancelAlways(event, event.getWorld());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onWeatherChange(final WeatherChangeEvent event) {
        cancelAlways(event, event.getWorld());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBedEnter(final PlayerBedEnterEvent event) {
        cancel(event, true, event.getPlayer().getWorld(), getMatch().getPlayer(event.getPlayer()), new TranslatableComponent("match.bed.disabled"));
    }


    // ---------------------------
    // -- Player item/block use --
    // ---------------------------

    // This handler listens on HIGHEST so that other plugins get a chance
    // to handle observer clicks before we cancel them i.e. WorldEdit.
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(final PlayerInteractEvent event) {
        if(cancelUnlessInteracting(event, event.getPlayer())) {
            // Allow the how-to book to be read
            if(event.getMaterial() == Material.WRITTEN_BOOK) {
                event.setUseItemInHand(Event.Result.ALLOW);
            } else {
                event.setUseItemInHand(Event.Result.DENY);
                event.setUseInteractedBlock(Event.Result.DENY);
            }

            MatchPlayer player = getMatch().getPlayer(event.getPlayer());
            if(player == null) return;

            if(!player.isSpawned()) {
                ClickType clickType = convertClick(event.getAction(), event.getPlayer());
                if(clickType == null) return;

                getMatch().callEvent(new ObserverInteractEvent(player, clickType, event.getClickedBlock(), null, event.getItem()));
            }

            // Right-clicking armor will put it on unless we do this
            event.getPlayer().updateInventory();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onShoot(final EntityShootBowEvent event) {
        // PlayerInteractEvent is fired on draw, this is fired on release. Need to cancel both.
        cancelUnlessInteracting(event, event.getEntity());
    }


    // --------------------------------------
    // -- Player interaction with entities --
    // --------------------------------------

    void callObserverInteractEvent(PlayerInteractEntityEvent event) {
        MatchPlayer player = getMatch().getPlayer(event.getPlayer());
        if(player == null || player.isSpawned()) return;

        getMatch().callEvent(new ObserverInteractEvent(player,
                                                       convertClick(ClickType.RIGHT, event.getPlayer()),
                                                       null,
                                                       event.getRightClicked(),
                                                       event.getPlayer().getInventory().getItem(event.getHand())));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityInteract(final PlayerInteractEntityEvent event) {
        if(cancelUnlessInteracting(event, event.getPlayer())) {
            callObserverInteractEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onArmorStandInteract(final PlayerInteractAtEntityEvent event) {
        cancelUnlessInteracting(event, event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onArmorStandInteract(final PlayerArmorStandManipulateEvent event) {
        cancelUnlessInteracting(event, event.getPlayer());
    }


    // --------------------------------------
    // -- Player interaction with vehicles --
    // --------------------------------------

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVehicleDamage(final VehicleDamageEvent event) {
        cancelUnlessInteracting(event, event.getAttacker());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVehiclePush(final VehicleEntityCollisionEvent event) {
        cancelUnlessInteracting(event, event.getEntity());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVehicleEnter(final VehicleEnterEvent event) {
        cancelUnlessInteracting(event, event.getEntered());
    }


    // ------------------------------------
    // -- Player interaction with blocks --
    // ------------------------------------

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerBlockChange(final PlayerBlockTransformEvent event) {
        cancelUnlessInteracting(event, event.getPlayerState());

        if(!event.isCancelled() && event.getNewState().getType() == Material.ENDER_CHEST) {
            cancel(event, true, event.getWorld(), event.getPlayer(), new TranslatableComponent("match.enderChestsDisabled"));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerBlockDamage(final BlockDamageEvent event) {
        cancelUnlessInteracting(event, event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHangingBreak(final HangingBreakEvent event) {
        cancelUnlessInteracting(event, event instanceof HangingBreakByEntityEvent ? ((HangingBreakByEntityEvent) event).getRemover()
                                                                                  : null);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAdventureModeInteract(final AdventureModeInteractEvent event) {
        cancelUnlessInteracting(event, event.getActor());
    }


    // --------------------------
    // -- Player damage/combat --
    // --------------------------
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAttack(final PlayerAttackEntityEvent event) {
        if(cancelUnlessInteracting(event, event.getPlayer())) {
            final MatchPlayer attacker = getMatch().getPlayer(event.getPlayer());
            if(attacker == null || attacker.isSpawned()) return;
            getMatch().callEvent(new ObserverInteractEvent(attacker, ClickType.LEFT, null, event.getLeftClicked(), event.getPlayer().getInventory().getItemInMainHand()));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDamage(final EntityDamageEvent event) {
        cancelUnlessInteracting(event, event.getEntity());
        if(event instanceof EntityDamageByEntityEvent) {
            cancelUnlessInteracting(event, ((EntityDamageByEntityEvent) event).getDamager());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCombust(final EntityCombustEvent event) {
        cancelUnlessInteracting(event, event.getEntity());
        if(event instanceof EntityCombustByEntityEvent) {
            cancelUnlessInteracting(event, ((EntityCombustByEntityEvent) event).getCombuster());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPotionSplash(final PotionSplashEvent event) {
        for(LivingEntity entity : event.getAffectedEntities()) {
            if(!getMatch().canInteract(entity)) {
                event.setIntensity(entity, 0);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPotionLinger(final AreaEffectCloudApplyEvent event) {
        for(Iterator<LivingEntity> iterator = event.getAffectedEntities().iterator(); iterator.hasNext(); ) {
            if(!getMatch().canInteract(iterator.next())) {
                iterator.remove();
            }
        }
    }


    // -----------------------------------
    // -- Player item/inventory actions --
    // -----------------------------------

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDropItem(final PlayerDropItemEvent event) {
        match.player(event.getPlayer()).ifPresent(player -> {
            if(!player.canInteract()) {
                if(player.isSpawned()) {
                    // If player is spawned (but frozen), force them to keep the item
                    event.setCancelled(true);
                } else {
                    // If player is observing, just destroy the item
                    event.getItemDrop().remove();
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerPickupItem(final PlayerPickupItemEvent event) {
        cancelUnlessInteracting(event, event.getPlayer());
    }


    // ----------------------
    // -- Player targeting --
    // ----------------------

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityTrack(final EntityTargetEvent event) {
        // Handles mobs and XP orbs
        if(event.getTarget() != null) cancelUnlessInteracting(event, event.getTarget());
    }
}
