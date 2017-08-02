package tc.oc.pgm.filters.matcher;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.inject.assistedinject.Assisted;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EntityAction;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import tc.oc.commons.bukkit.event.BlockPunchEvent;
import tc.oc.commons.bukkit.event.BlockTrampleEvent;
import tc.oc.commons.bukkit.event.GeneralizingEvent;
import tc.oc.pgm.filters.query.IEventQuery;
import tc.oc.pgm.tracker.EventResolver;
import tc.oc.pgm.tracker.damage.DamageInfo;
import tc.oc.pgm.tracker.damage.ItemInfo;
import tc.oc.pgm.tracker.damage.MeleeInfo;
import tc.oc.pgm.tracker.damage.PhysicalInfo;
import tc.oc.pgm.tracker.damage.PotionInfo;
import tc.oc.pgm.tracker.damage.ProjectileInfo;

public class CauseFilter extends TypedFilter.Impl<IEventQuery> {

    // TODO: add other causes like growth, flow, mob, machine, etc.
    public enum Cause {
        // Actor types
        WORLD, LIVING, MOB, PLAYER,

        // Block actions
        PUNCH, TRAMPLE, MINE,

        // Damage types
        MELEE, PROJECTILE, POTION, EXPLOSION, COMBUSTION, FALL, GRAVITY, VOID,
        SQUASH, SUFFOCATION, DROWNING, STARVATION, LIGHTNING, CACTUS, THORNS;
    }

    public interface Factory {
        CauseFilter create(Cause cause);
    }

    private final @Inspect Cause cause;
    private final Provider<EventResolver> eventResolverProvider;

    @Inject CauseFilter(@Assisted Cause cause, Provider<EventResolver> eventResolverProvider) {
        this.cause = cause;
        this.eventResolverProvider = eventResolverProvider;
    }

    public boolean matches(IEventQuery query) {
        Event event = query.getEvent();
        if(event instanceof GeneralizingEvent) {
            event = ((GeneralizingEvent) event).getCause();
        }

        EntityDamageEvent.DamageCause damageCause = null;
        DamageInfo damageInfo = null;
        boolean punchDamage = false;
        if(event instanceof EntityDamageEvent) {
            EntityDamageEvent damageEvent = (EntityDamageEvent) event;
            damageCause = damageEvent.getCause();
            damageInfo = eventResolverProvider.get().resolveDamage(damageEvent);
            if(damageInfo instanceof MeleeInfo) {
                PhysicalInfo weapon = ((MeleeInfo) damageInfo).getWeapon();
                if(weapon instanceof ItemInfo && ((ItemInfo) weapon).getItem().getType() == Material.AIR) {
                    punchDamage = true;
                }
            }
        }

        Entity actor = null;
        if(event instanceof EntityAction) {
            actor = ((EntityAction) event).getActor();
        }

        switch(this.cause) {
            // Actor types
            case WORLD:
                return !(actor instanceof LivingEntity);

            case LIVING:
                return actor instanceof LivingEntity;

            case MOB:
                return actor instanceof LivingEntity && !(actor instanceof Player);

            case PLAYER:
                return actor instanceof Player;

            // Block actions
            case PUNCH:
                return event instanceof BlockPunchEvent || punchDamage;

            case TRAMPLE:
                return event instanceof BlockTrampleEvent;

            case MINE:
                return event instanceof BlockDamageEvent ||
                       event instanceof BlockBreakEvent ||
                       event instanceof PlayerBucketFillEvent;

            // Damage types
            case MELEE:
                return damageCause == EntityDamageEvent.DamageCause.ENTITY_ATTACK ||
                       damageInfo instanceof MeleeInfo;

            case PROJECTILE:
                return damageCause == EntityDamageEvent.DamageCause.PROJECTILE ||
                       damageInfo instanceof ProjectileInfo;

            case POTION:
                return damageCause == EntityDamageEvent.DamageCause.MAGIC ||
                       damageCause == EntityDamageEvent.DamageCause.POISON ||
                       damageCause == EntityDamageEvent.DamageCause.WITHER ||
                       damageInfo instanceof PotionInfo;

            case EXPLOSION:
                return event instanceof EntityExplodeEvent ||
                       damageCause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION ||
                       damageCause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION;

            case COMBUSTION:
                return event instanceof BlockBurnEvent ||
                       damageCause == EntityDamageEvent.DamageCause.FIRE ||
                       damageCause == EntityDamageEvent.DamageCause.FIRE_TICK ||
                       damageCause == EntityDamageEvent.DamageCause.LAVA;

            case FALL: // Strictly damage from hitting the ground
                return damageCause == EntityDamageEvent.DamageCause.FALL;

            case GRAVITY: // Any damage caused by a fall
                return damageCause == EntityDamageEvent.DamageCause.FALL ||
                       damageCause == EntityDamageEvent.DamageCause.VOID;

            case VOID:
                return damageCause == EntityDamageEvent.DamageCause.VOID;

            case SQUASH:
                return damageCause == EntityDamageEvent.DamageCause.FALLING_BLOCK;

            case SUFFOCATION:
                return damageCause == EntityDamageEvent.DamageCause.SUFFOCATION;

            case DROWNING:
                return damageCause == EntityDamageEvent.DamageCause.DROWNING;

            case STARVATION:
                return damageCause == EntityDamageEvent.DamageCause.STARVATION;

            case LIGHTNING:
                return damageCause == EntityDamageEvent.DamageCause.LIGHTNING;

            case CACTUS:
                return damageCause == EntityDamageEvent.DamageCause.CONTACT;

            case THORNS:
                return damageCause == EntityDamageEvent.DamageCause.THORNS;

            default:
                return false;
        }
    }
}
