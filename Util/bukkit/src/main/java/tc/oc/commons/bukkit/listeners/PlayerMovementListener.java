package tc.oc.commons.bukkit.listeners;

import java.util.Map;
import java.util.WeakHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.EntityLocation;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventBus;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.RayBlockIntersection;
import org.bukkit.util.Vector;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.event.BlockPunchEvent;
import tc.oc.commons.bukkit.event.BlockTrampleEvent;
import tc.oc.commons.bukkit.event.CoarsePlayerMoveEvent;
import tc.oc.commons.bukkit.util.BlockUtils;
import tc.oc.commons.bukkit.util.Materials;
import tc.oc.commons.core.plugin.PluginFacet;

/**
 * Translates standard Bukkit events into a few extra events:
 *   {@link CoarsePlayerMoveEvent}
 *   {@link BlockPunchEvent}
 *   {@link BlockTrampleEvent}
 */
@Singleton
public class PlayerMovementListener implements PluginFacet, Listener {

    protected final EventBus eventBus;
    protected final Audiences audiences;

    // The last location of a player that has been used to generate
    // coarse movement events. If a player is not in this list, then
    // the next movement event they generate can be assumed valid
    // on its own.
    private final Map<Player, EntityLocation> lastToLocation = new WeakHashMap<>();

    @Inject PlayerMovementListener(EventBus eventBus, Audiences audiences) {
        this.eventBus = eventBus;
        this.audiences = audiences;
    }

    /**
     * Update the last known location of a player to account for the
     * given movement event
     */
    private void updateLastToLocation(final PlayerMoveEvent event) {
        if(event.isCancelled()) {
            this.lastToLocation.put(event.getPlayer(), event.getEntityFrom());
        } else {
            this.lastToLocation.put(event.getPlayer(), event.getEntityTo());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        lastToLocation.remove(event.getPlayer());
    }

    // -------------------------
    // ---- Player movement ----
    // -------------------------

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMoveHigh(final PlayerMoveEvent event) {
        this.handleMovementHigh(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleportHigh(final PlayerTeleportEvent event) {
        this.handleMovementHigh(event);
    }

    private final void handleMovementHigh(final PlayerMoveEvent event) {
        Player player = event.getPlayer();
        EntityLocation originalFrom = event.getEntityFrom();
        EntityLocation originalTo = event.getEntityTo();

        EntityLocation oldTo = this.lastToLocation.get(player);
        if(oldTo != null && !oldTo.equals(originalFrom)) {
            // If this movement does not start where the last known movement ended,
            // we have to make up the missing movement. We do that by (potentially) firing
            // two coarse events for this one event, a "fake" one for the missing movement
            // and a "real" one for the current movement.

            // First, modify this event to look like the missing event, and fire
            // a coarse event that wraps it.
            event.setFrom(oldTo);
            event.setTo(originalFrom);
            this.updateLastToLocation(event);
            if(this.callCoarsePlayerMove(event)) {
                // If the fake coarse event was cancelled, we don't need to fire
                // the real one, so just return. Note that the wrapped event won't
                // actually be cancelled, rather its to location will be modified
                // to return the player to the oldTo location. Also note that if
                // the original event was already cancelled before the coarse event
                // fired, then we will never get here, and both the fake and real
                // events will go through.
                this.updateLastToLocation(event);
                return;
            }

            // Restore the event to its real state
            event.setFrom(originalFrom);
            event.setTo(originalTo);
        }

        this.updateLastToLocation(event);
        if(this.callCoarsePlayerMove(event)) {
            this.updateLastToLocation(event);
        }
    }

    /**
     * Fire a CoarsePlayerMoveEvent that wraps the given event, only if it crosses
     * a block boundary, or the PoseFlags change.
     * @param event         The movement event to potentially wrap
     * @return True if the original event was not cancelled, and a coarse event was fired,
     *         and that coarse event was cancelled. In this case, the wrapped event won't
     *         actually be cancelled, but callers should treat it like it is.
     */
    private boolean callCoarsePlayerMove(final PlayerMoveEvent event) {
        // Don't fire coarse events for teleports that are not "in-game"
        // e.g. /jumpto command
        if(event instanceof PlayerTeleportEvent) {
            PlayerTeleportEvent teleportEvent = (PlayerTeleportEvent) event;
            if(teleportEvent.getCause() != TeleportCause.ENDER_PEARL &&
               teleportEvent.getCause() != TeleportCause.UNKNOWN) {
                return false;
            }
        }

        // If the movement does not cross a block boundary, and no PoseFlags changed, we don't care about it
        final EntityLocation from = event.getEntityFrom();
        final EntityLocation to = event.getEntityTo();
        if(from.position().coarseEquals(to.position()) && from.poseFlags().equals(to.poseFlags())) {
            return false;
        }

        // Remember whether the original event was already cancelled
        boolean wasCancelled = event.isCancelled();

        CoarsePlayerMoveEvent generalEvent = new CoarsePlayerMoveEvent(event, event.getPlayer(), from, to);
        this.eventBus.callEvent(generalEvent);

        if(!wasCancelled && generalEvent.isCancelled()) {
            // When a coarse event is cancelled, we have our own logic for resetting the
            // player's position, so we un-cancel the event and instead modify its
            // to location to put the player where we want them.
            resetPosition(event);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Modify the to location of the given event to prevent the movement and
     * move the player so they are standing on the center of the block at the
     * from location.
     */
    private static void resetPosition(final PlayerMoveEvent event) {
        Location newLoc;
        double yValue = event.getFrom().getY();

        if(yValue <= 0 || event instanceof PlayerTeleportEvent) {
            newLoc = event.getFrom();
        } else {
            newLoc = BlockUtils.center(event.getFrom()).subtract(new Vector(0, 0.5, 0));
            if(newLoc.getBlock() != null) {
                switch(newLoc.getBlock().getType()) {
                case STEP:
                case WOOD_STEP:
                    newLoc.add(new Vector(0, 0.5, 0));
                    break;
                default: break;
                }
            }
        }

        newLoc.setPitch(event.getTo().getPitch());
        newLoc.setYaw(event.getTo().getYaw());
        event.setCancelled(false);
        event.setTo(newLoc);
    }

    // reset the last location on death
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        this.lastToLocation.remove(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMoveMonitor(final PlayerMoveEvent event) {
        this.handleMovementMonitor(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleportMonitor(final PlayerTeleportEvent event) {
        this.handleMovementMonitor(event);
    }

    private void handleMovementMonitor(PlayerMoveEvent event) {
        // It's possible for a PlayerMoveEvent to be modified by another
        // HIGHEST handler after we handle it, so we also check it at MONITOR
        this.updateLastToLocation(event);
    }

    // ------------------------------------------
    // ---- Adventure mode block interaction ----
    // ------------------------------------------

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void detectBlockPunch(PlayerAnimationEvent event) {
        if(event.getAnimationType() != PlayerAnimationType.ARM_SWING) return;
        if(event.getPlayer().getGameMode() != GameMode.ADVENTURE) return;

        // Client will not punch blocks in adventure mode, so we detect it ourselves and fire a BlockPunchEvent.
        // We do this in the kit module only because its the one that is responsible for putting players in adventure mode.
        // A few other modules rely on this, including StaminaModule and BlockDropsModule.
        RayBlockIntersection hit = event.getPlayer().getTargetedBlock(true, false);
        if(hit == null) return;

        eventBus.callEvent(new BlockPunchEvent(event.getPlayer(), hit));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void detectBlockTrample(CoarsePlayerMoveEvent event) {
        if(!event.getPlayer().isOnGround()) return;

        Block block = event.getBlockTo().getBlock();
        if(!Materials.isColliding(block.getType())) {
            block = block.getRelative(BlockFace.DOWN);
            if(!Materials.isColliding(block.getType())) return;
        }

        eventBus.callEvent(new BlockTrampleEvent(event.getPlayer(), block));
    }

    // -------------------------
    // ---- Cancel messages ----
    // -------------------------

    @EventHandler(priority = EventPriority.MONITOR)
    public void processCancelMessage(final CoarsePlayerMoveEvent event) {
        if(event.isCancelled() && event.getCancelMessage() != null) {
            audiences.get(event.getPlayer()).sendWarning(event.getCancelMessage(), false);
        }
    }
}
