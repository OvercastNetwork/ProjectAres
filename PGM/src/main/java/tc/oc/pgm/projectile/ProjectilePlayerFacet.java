package tc.oc.pgm.projectile;

import java.util.logging.Level;
import javax.inject.Inject;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;
import tc.oc.commons.bukkit.event.targeted.TargetedEventHandler;
import tc.oc.commons.bukkit.logging.MapdevLogger;
import tc.oc.commons.core.util.CheckedCloseable;
import tc.oc.commons.core.util.TimeUtils;
import tc.oc.pgm.PGM;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayerFacet;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.ParticipantState;

import static com.google.common.base.Preconditions.checkState;

@ListenerScope(MatchScope.RUNNING)
public class ProjectilePlayerFacet implements MatchPlayerFacet, Listener {

    private final Match match;
    private final Player player;
    private final MapdevLogger mapdevLogger;

    @Inject ProjectilePlayerFacet(Match match, Player player, MapdevLogger mapdevLogger) {
        this.match = match;
        this.player = player;
        this.mapdevLogger = mapdevLogger;
    }

    @TargetedEventHandler(priority = EventPriority.HIGH)
    public void onClickEvent(PlayerInteractEvent event) {
        if(event.getAction() == Action.PHYSICAL) return;

        final ParticipantState playerState = match.getParticipantState(player);
        if(playerState == null) return;

        final ItemStack item = event.getItem();
        final ProjectileDefinition definition = Projectiles.getProjectileDefinition(match.featureDefinitions(), item);
        if(definition == null || !isValidProjectileAction(event.getAction(), definition.clickAction())) return;

        // Prevent the original projectile from being fired
        event.setCancelled(true);

        if(definition.cooldown() != null && player.getRemainingItemCooldown(item.getType()) > 0F) return;

        final boolean realProjectile = Projectile.class.isAssignableFrom(definition.projectile());
        final Vector velocity = player.getEyeLocation().getDirection().multiply(definition.velocity());
        final Entity projectile;

        checkState(Projectiles.launchingDefinition.get() == null, "nested projectile launch");
        try(CheckedCloseable x = Projectiles.launchingDefinition.let(definition)) {
            try {
                if(realProjectile) {
                    projectile = player.launchProjectile(definition.projectile().asSubclass(Projectile.class), velocity);
                } else {
                    projectile = player.getWorld().spawn(player.getEyeLocation(), definition.projectile());
                    projectile.setVelocity(velocity);
                }
            } catch(Exception e) {
                mapdevLogger.log(Level.SEVERE, "Failed to spawn custom projectile type " + definition.projectile().getSimpleName(), e);
                return;
            }

            projectile.setMetadata(Projectiles.METADATA_KEY, new FixedMetadataValue(PGM.get(), definition));
        }

        if (projectile instanceof Arrow && item.getEnchantments().containsKey(Enchantment.ARROW_INFINITE)) {
            ((Arrow) projectile).setPickupStatus(Arrow.PickupStatus.DISALLOWED);
        }

        // If the entity implements Projectile, it will have already generated a ProjectileLaunchEvent.
        // Otherwise, we fire our custom event.
        if(!realProjectile) {
            EntityLaunchEvent launchEvent = new EntityLaunchEvent(projectile, event.getPlayer());
            match.callEvent(launchEvent);
            if(launchEvent.isCancelled()) {
                projectile.remove();
                return;
            }
        }

        if(definition.throwable()) {
            if(item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.getInventory().setItem(event.getHand(), null);
            }
        }

        if(definition.cooldown() != null) {
            player.startItemCooldown(item.getType(), (int) TimeUtils.toTicks(definition.cooldown()));
        }
    }

    private static boolean isValidProjectileAction(Action action, ClickAction clickAction) {
        switch(clickAction) {
            case RIGHT:
                return action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
            case LEFT:
                return action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK;
            case BOTH:
                return action != Action.PHYSICAL;
        }
        return false;
    }
}
