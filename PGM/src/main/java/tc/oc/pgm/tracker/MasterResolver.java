package tc.oc.pgm.tracker;

import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;
import tc.oc.pgm.match.MatchPlayerFinder;
import tc.oc.pgm.tracker.damage.DamageInfo;
import tc.oc.pgm.tracker.damage.NullDamageInfo;
import tc.oc.pgm.tracker.damage.PhysicalInfo;
import tc.oc.pgm.tracker.resolvers.DamageResolver;
import tc.oc.pgm.tracker.trackers.BlockTracker;
import tc.oc.pgm.tracker.trackers.EntityTracker;

class MasterResolver implements EventResolver, ProjectileResolver {

    private final MatchPlayerFinder players;
    private final EntityTracker entityTracker;
    private final BlockTracker blockTracker;
    private final Set<DamageResolver> damageResolvers;

    @Inject MasterResolver(MatchPlayerFinder players, EntityTracker entityTracker, BlockTracker blockTracker, Set<DamageResolver> damageResolvers) {
        this.players = players;
        this.entityTracker = entityTracker;
        this.blockTracker = blockTracker;
        this.damageResolvers = damageResolvers;
    }

    @Override
    public DamageInfo resolveDamage(EntityDamageEvent.DamageCause damageType, Entity victim, @Nullable Block damager) {
        if(damager == null) return resolveDamage(damageType, victim);
        return resolveDamage(damageType, victim, blockTracker.resolveBlock(damager));

    }

    @Override
    public DamageInfo resolveDamage(EntityDamageEvent.DamageCause damageType, Entity victim, @Nullable Entity damager) {
        if(damager == null) return resolveDamage(damageType, victim);
        return resolveDamage(damageType, victim, entityTracker.resolveEntity(damager));
    }

    @Override
    public DamageInfo resolveDamage(EntityDamageEvent.DamageCause damageType, Entity victim, @Nullable PhysicalInfo damager) {
        // Filter out observers immediately
        if(!players.canInteract(victim)) return new NullDamageInfo();

        for(DamageResolver resolver : damageResolvers) {
            DamageInfo resolvedInfo = resolver.resolveDamage(damageType, victim, damager);
            if(resolvedInfo != null) {
                return resolvedInfo;
            }
        }

        // This should never happen
        return new NullDamageInfo();
    }

    @Override
    public @Nullable PhysicalInfo resolveShooter(ProjectileSource source) {
        if(source instanceof Entity) {
            return entityTracker.resolveEntity((Entity) source);
        } else if(source instanceof BlockProjectileSource) {
            return blockTracker.resolveBlock(((BlockProjectileSource) source).getBlock());
        }
        return null;
    }
}
