package tc.oc.pgm.tnt;

import java.util.Random;
import javax.annotation.Nullable;
import javax.inject.Inject;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeByEntityEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import tc.oc.commons.bukkit.util.BlockUtils;
import tc.oc.pgm.events.BlockTransformEvent;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.projectile.EntityLaunchEvent;

import static com.google.common.base.Preconditions.checkNotNull;

@ListenerScope(MatchScope.RUNNING)
public class TNTMatchModule extends MatchModule implements Listener {
    private final TNTProperties properties;

    @Inject private TNTMatchModule(TNTProperties properties) {
        this.properties = properties;
    }

    public TNTProperties getProperties() {
        return properties;
    }

    public int getFuseTicks() {
        assert this.properties.fuse != null;
        return (int) (this.properties.fuse.toMillis() / 50.0);
    }

    private boolean callPrimeEvent(TNTPrimed tnt, @Nullable Entity primer, boolean instant) {
        final ExplosionPrimeEvent event;
        if(instant) {
            event = new InstantTNTPlaceEvent(tnt, checkNotNull(primer));
        } else if(primer != null) {
            event = new ExplosionPrimeByEntityEvent(tnt, primer);
        } else {
            event = new ExplosionPrimeEvent(tnt);
        }

        getMatch().callEvent(event);
        if(event.isCancelled()) {
            tnt.remove();
            return false;
        } else {
            return true;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void yieldSet(EntityExplodeEvent event) {
        if(this.properties.yield != null && event.getEntity() instanceof TNTPrimed) {
            event.setYield(this.properties.yield);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleInstantActivation(BlockPlaceEvent event) {
        if(this.properties.instantIgnite && event.getBlock().getType() == Material.TNT) {
            World world = event.getBlock().getWorld();
            TNTPrimed tnt = world.spawn(BlockUtils.base(event.getBlock()), TNTPrimed.class);

            if(this.properties.fuse != null) {
                tnt.setFuseTicks(this.getFuseTicks());
            }

            if(this.properties.power != null) {
                tnt.setYield(this.properties.power); // Note: not related to EntityExplodeEvent.yield
            }

            if(callPrimeEvent(tnt, event.getPlayer(), true)) {
                // Only cancel the block placement if the prime event is NOT cancelled.
                // If priming is cancelled, the block is allowed to stay (unless some
                // other handler has already cancelled the place event).
                event.setCancelled(true);
                world.playSound(tnt.getLocation(), Sound.ENTITY_TNT_PRIMED, 1, 1);

                ItemStack inHand = event.getItemInHand();
                if(inHand.getAmount() == 1) {
                    inHand = null;
                } else {
                    inHand.setAmount(inHand.getAmount() - 1);
                }
                event.getPlayer().getInventory().setItem(event.getHand(), inHand);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleInstantActivation(EntityLaunchEvent event) {
        if(event.getEntity() instanceof TNTPrimed) {
            TNTPrimed tnt = (TNTPrimed) event.getEntity();

            if(this.properties.fuse != null) {
                tnt.setFuseTicks(this.getFuseTicks());
            }

            if(this.properties.power != null) {
                tnt.setYield(this.properties.power); // Note: not related to EntityExplodeEvent.yield
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void setCustomProperties(ExplosionPrimeEvent event) {
        if(event.getEntity() instanceof TNTPrimed) {
            TNTPrimed tnt = (TNTPrimed) event.getEntity();

            if(this.properties.fuse != null) {
                tnt.setFuseTicks(this.getFuseTicks());
            }

            if(this.properties.power != null) {
                tnt.setYield(this.properties.power); // Note: not related to EntityExplodeEvent.yield
            }
        }
    }

    // Make sure this event handler is called before the one in DispenserTracker that clears the placer
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void dispenserNukes(BlockTransformEvent event) {
        BlockState oldState = event.getOldState();
        if(oldState instanceof Dispenser &&
           this.properties.dispenserNukeLimit > 0 &&
           this.properties.dispenserNukeMultiplier > 0 &&
           event.getCause() instanceof EntityExplodeEvent) {

            EntityExplodeEvent explodeEvent = (EntityExplodeEvent) event.getCause();
            Dispenser dispenser = (Dispenser) oldState;
            int tntLimit = Math.round(this.properties.dispenserNukeLimit / this.properties.dispenserNukeMultiplier);
            int tntCount = 0;

            for(ItemStack stack : dispenser.getInventory().contents()) {
                if(stack != null && stack.getType() == Material.TNT) {
                    int transfer = Math.min(stack.getAmount(), tntLimit - tntCount);
                    if(transfer > 0) {
                        stack.setAmount(stack.getAmount() - transfer);
                        tntCount += transfer;
                    }
                }
            }

            tntCount = (int) Math.ceil(tntCount * this.properties.dispenserNukeMultiplier);

            for(int i = 0; i < tntCount; i++) {
                TNTPrimed tnt = this.getMatch().getWorld().spawn(BlockUtils.base(dispenser), TNTPrimed.class);

                tnt.setFuseTicks(10 + this.getMatch().getRandom().nextInt(10)); // between 0.5 and 1.0 seconds, same as vanilla TNT chaining

                Random random = this.getMatch().getRandom();
                Vector velocity = new Vector(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()); // uniform random direction
                velocity.normalize().multiply(0.5 + 0.5 * random.nextDouble());
                tnt.setVelocity(velocity);

                callPrimeEvent(tnt, explodeEvent.getEntity(), false);
            }
        }
    }
}
