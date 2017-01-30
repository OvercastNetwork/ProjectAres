package tc.oc.pgm.effect;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.metadata.FixedMetadataValue;
import tc.oc.pgm.PGM;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.Repeatable;
import tc.oc.pgm.utils.EntityUtils;

/**
 * Change the default projectile particle trails with a colored
 * trail that matches the player's party color.
 */
@ListenerScope(MatchScope.RUNNING)
public class ProjectileTrailMatchModule extends MatchModule implements Listener {

    private static final String TRAIL_META = "projectile_trail_color";
    private static final String CRITICAL_META = "arrow_is_critical";

    @Repeatable(scope = MatchScope.RUNNING)
    public void tick() {
        EntityUtils.entities(match.getWorld(), Projectile.class)
             .filter(projectile -> projectile.hasMetadata(TRAIL_META))
             .forEach(projectile -> {
                 if(projectile.isDead() || projectile.isOnGround()) {
                     projectile.removeMetadata(TRAIL_META, PGM.get());
                 } else {
                     final Color color = (Color) projectile.getMetadata(TRAIL_META, PGM.get()).value();
                     // Certain particles can have a specific color if:
                     // - Count is 0
                     // - Speed is 1
                     // - Delta vectors are RGB values from (0,1]
                     match.getWorld().spawnParticle(
                         Particle.REDSTONE,
                         projectile.getLocation(),
                         0,
                         rgbToParticle(color.getRed()),
                         rgbToParticle(color.getGreen()),
                         rgbToParticle(color.getBlue()),
                         1
                     );
                 }
             });
    }

    private double rgbToParticle(int rgb) {
        return Math.max(0.001, rgb / 255.0);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        match.player(event.getActor()).ifPresent(shooter -> {
            final Projectile projectile = event.getEntity();
            projectile.setMetadata(TRAIL_META, new FixedMetadataValue(PGM.get(), shooter.getParty().getFullColor()));
            // Set critical metadata to false in order to remove default particle trail.
            // The metadata will be restored just before the arrow hits something.
            if(projectile instanceof Arrow) {
                final Arrow arrow = (Arrow) projectile;
                arrow.setMetadata(CRITICAL_META, new FixedMetadataValue(PGM.get(), arrow.isCritical()));
                arrow.setCritical(false);
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        match.player(event.getActor()).ifPresent(shooter -> {
            final Projectile projectile = event.getEntity();
            projectile.removeMetadata(TRAIL_META, PGM.get());
            // Restore critical metadata to arrows if applicable
            if(projectile instanceof Arrow) {
                final Arrow arrow = (Arrow) projectile;
                if(arrow.hasMetadata(CRITICAL_META)) {
                    arrow.setCritical(arrow.getMetadata(CRITICAL_META, PGM.get()).asBoolean());
                }
            }
        });
    }

}
