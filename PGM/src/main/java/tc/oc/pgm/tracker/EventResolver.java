package tc.oc.pgm.tracker;

import javax.annotation.Nullable;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import tc.oc.pgm.tracker.damage.DamageInfo;
import tc.oc.pgm.tracker.damage.PhysicalInfo;
import tc.oc.pgm.tracker.resolvers.DamageResolver;

public interface EventResolver extends DamageResolver {

    default DamageInfo resolveDamage(EntityDamageEvent damageEvent) {
        if(damageEvent instanceof EntityDamageByEntityEvent) {
            return resolveDamage((EntityDamageByEntityEvent) damageEvent);
        } else if(damageEvent instanceof EntityDamageByBlockEvent) {
            return resolveDamage((EntityDamageByBlockEvent) damageEvent);
        } else {
            return resolveDamage(damageEvent.getCause(), damageEvent.getEntity());
        }
    }

    default DamageInfo resolveDamage(EntityDamageByEntityEvent damageEvent) {
        return resolveDamage(damageEvent.getCause(), damageEvent.getEntity(), damageEvent.getDamager());
    }

    default DamageInfo resolveDamage(EntityDamageByBlockEvent damageEvent) {
        return resolveDamage(damageEvent.getCause(), damageEvent.getEntity(), damageEvent.getDamager());
    }

    default DamageInfo resolveDamage(EntityDamageEvent.DamageCause damageType, Entity victim) {
        return resolveDamage(damageType, victim, (PhysicalInfo) null);
    }

    DamageInfo resolveDamage(EntityDamageEvent.DamageCause damageType, Entity victim, @Nullable Block damager);

    DamageInfo resolveDamage(EntityDamageEvent.DamageCause damageType, Entity victim, @Nullable Entity damager);
}

