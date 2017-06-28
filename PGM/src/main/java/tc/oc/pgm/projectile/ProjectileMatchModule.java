package tc.oc.pgm.projectile;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.BlockIterator;
import tc.oc.pgm.events.BlockTransformEvent;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.query.BlockEventQuery;
import tc.oc.pgm.filters.query.IQuery;
import tc.oc.pgm.filters.query.PlayerBlockEventQuery;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;

@ListenerScope(MatchScope.RUNNING)
public class ProjectileMatchModule extends MatchModule implements Listener {

    @EventHandler
    public void onProjectileHurtEvent(EntityDamageByEntityEvent event) {
        if(!(event.getEntity() instanceof LivingEntity)) return;
        final LivingEntity damagedEntity = (LivingEntity) event.getEntity();

        final ProjectileDefinition projectileDefinition = Projectiles.getProjectileDefinition(event.getDamager());
        if(projectileDefinition == null) return;

        if(!projectileDefinition.potion().isEmpty()) {
            damagedEntity.addPotionEffects(projectileDefinition.potion());
        }

        if(projectileDefinition.damage() != null) {
            event.setDamage(projectileDefinition.damage());
        }

        if (projectileDefinition.kit() != null) {
            if (event.getEntity() instanceof Player) {
                projectileDefinition.kit().apply(match.getPlayer((Player)event.getEntity()));
            }
        }
    }

    @EventHandler
    public void onProjectileHitEvent(ProjectileHitEvent event) {
        final Projectile projectile = event.getEntity();
        final ProjectileDefinition projectileDefinition = Projectiles.getProjectileDefinition(projectile);
        if(projectileDefinition == null) return;

        final Filter filter = projectileDefinition.destroyFilter();
        if(filter == null) return;

        final BlockIterator blockIterator = new BlockIterator(projectile.getWorld(), projectile.getLocation().toVector(), projectile.getVelocity().normalize(), 0d, 2);
        Block hitBlock = null;
        while(blockIterator.hasNext()) {
            hitBlock = blockIterator.next();
            if(hitBlock.getType() != Material.AIR) break;
        }

        if(hitBlock != null) {
            final MatchPlayer shooter = projectile.getShooter() instanceof Player ? getMatch().getPlayer((Player) projectile.getShooter()) : null;
            final IQuery query = shooter != null ? new PlayerBlockEventQuery(shooter, event, hitBlock.getState())
                                                 : new BlockEventQuery(event, hitBlock);

            if(filter.query(query).isAllowed()) {
                final BlockTransformEvent bte = new BlockTransformEvent(event, hitBlock, Material.AIR);
                match.callEvent(bte);

                if(!bte.isCancelled()) {
                    hitBlock.setType(Material.AIR);
                    projectile.remove();
                }
            }
        }
    }
}
