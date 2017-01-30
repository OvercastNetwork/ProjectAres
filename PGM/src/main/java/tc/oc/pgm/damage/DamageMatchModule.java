package tc.oc.pgm.damage;

import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import javax.inject.Inject;

import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import tc.oc.commons.bukkit.util.PotionClassification;
import tc.oc.pgm.antigrief.AntiGrief;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.MatchPlayerDamageEvent;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.query.DamageQuery;
import tc.oc.pgm.filters.query.IDamageQuery;
import tc.oc.pgm.filters.query.IQuery;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.ParticipantState;
import tc.oc.pgm.match.PlayerRelation;
import tc.oc.pgm.tracker.EventResolver;
import tc.oc.pgm.tracker.EntityResolver;
import tc.oc.pgm.tracker.damage.DamageInfo;
import tc.oc.pgm.tracker.damage.EntityInfo;
import tc.oc.pgm.tracker.damage.ExplosionInfo;
import tc.oc.pgm.tracker.damage.FallInfo;
import tc.oc.pgm.tracker.damage.FireInfo;
import tc.oc.pgm.tracker.damage.GenericDamageInfo;
import tc.oc.pgm.tracker.damage.ProjectileInfo;

import static com.google.common.base.Preconditions.checkNotNull;

@ListenerScope(MatchScope.RUNNING)
public class DamageMatchModule extends MatchModule implements Listener {

    @Inject private EventResolver damageResolver;
    @Inject private EntityResolver entityResolver;

    private final List<Filter> filters;

    DamageMatchModule(List<Filter> filters) {
        this.filters = filters;
    }

    /**
     * Are players allowed to inflict the given damage on themselves?
     * Custom rules can override this.
     */
    public static boolean isAllowedSelfDamage(DamageInfo damageInfo) {
        // Disable self-damage with arrows
        if(damageInfo instanceof ProjectileInfo) {
            ProjectileInfo projectileInfo = (ProjectileInfo) damageInfo;
            if(projectileInfo.getProjectile() instanceof EntityInfo &&
               ((EntityInfo) projectileInfo.getProjectile()).getEntityType() == EntityType.ARROW) {
                return false;
            }
        }
        return true;
    }

    /**
     * Is the given damage of a type that is always allowed against teammates,
     * even if friendly fire is disabled? Custom rules can override this.
     */
    public static boolean isAllowedTeamDamage(DamageInfo damageInfo) {
        return damageInfo instanceof ExplosionInfo ||
               damageInfo instanceof FireInfo ||
               damageInfo instanceof FallInfo ||
               damageInfo instanceof GenericDamageInfo; // This should never have an attacker anyway, but just in case
    }

    /**
     * Test if the given damage/attack is allowed by the default damage policies
     */
    public Filter.QueryResponse queryDefaultRules(ParticipantState victim, DamageInfo damageInfo) {
        switch(PlayerRelation.get(victim, damageInfo.getAttacker())) {
            case SELF:
                if(!isAllowedSelfDamage(damageInfo)) {
                    return Filter.QueryResponse.DENY;
                }

            case ALLY:
                if(!match.getMapInfo().friendlyFire && !isAllowedTeamDamage(damageInfo)) {
                    return Filter.QueryResponse.DENY;
                }

            default:
                return Filter.QueryResponse.ABSTAIN;
        }
    }

    /**
     * Get a query for the given damage event
     */
    public IDamageQuery getQuery(Event event, ParticipantState victim, DamageInfo damageInfo) {
        return DamageQuery.victimDefault(event, victim, damageInfo);
    }

    /**
     * Query the custom damage filters with the given damage event
     */
    public Filter.QueryResponse queryRules(Event event, ParticipantState victim, DamageInfo damageInfo) {
        IQuery query = getQuery(event, victim, damageInfo);

        for(Filter filter : filters) {
            Filter.QueryResponse response = filter.query(query);
            if(response != Filter.QueryResponse.ABSTAIN) return response;
        }

        return Filter.QueryResponse.ABSTAIN;
    }

    /**
     * Query whether the given damage is both allowed and incentivized for the attacker.
     */
    public Filter.QueryResponse queryHostile(Event event, ParticipantState victim, DamageInfo damageInfo) {
        switch(PlayerRelation.get(victim, damageInfo.getAttacker())) {
            case SELF:
            case ALLY:
                // Players don't want to hurt themselves or their teammates
                return Filter.QueryResponse.DENY;

            default:
                // They also don't want to waste time trying to inflict damage that will be filtered out
                return queryRules(event, victim, damageInfo);
        }
    }

    /**
     * Query whether the given damage is allowed or not. Both the custom damage filters and the friendly fire policy
     * are considered, the former having priority over the latter.
     */
    public Filter.QueryResponse queryDamage(Event event, ParticipantState victim, DamageInfo damageInfo) {
        Filter.QueryResponse response = queryRules(event, victim, damageInfo);
        if(response != Filter.QueryResponse.ABSTAIN) return response;

        return queryDefaultRules(victim, damageInfo);
    }

    /**
     * Query the given damage event and cancel it if the result was denied.
     */
    public Filter.QueryResponse processDamageEvent(Event event, ParticipantState victim, DamageInfo damageInfo) {
        Filter.QueryResponse response = queryDamage(checkNotNull(event), victim, damageInfo);
        if(response.isDenied() && event instanceof Cancellable) {
            ((Cancellable) event).setCancelled(true);
        }
        return response;
    }

    /**
     * Search the rider stack for a participant
     */
    @Nullable MatchPlayer getVictim(Entity entity) {
        if(entity == null) return null;

        MatchPlayer victim = getMatch().getParticipant(entity);
        if(victim != null) {
            return victim;
        } else if(AntiGrief.VechicleProtect.enabled()) {
            return getVictim(entity.getPassenger());
        } else {
            return null;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        MatchPlayer victim = getVictim(event.getEntity());
        if(victim == null) return;
        final DamageInfo info = damageResolver.resolveDamage(event);
        processDamageEvent(event, victim.getParticipantState(), info);
        if(!event.isCancelled()) {
            getMatch().callEvent(new MatchPlayerDamageEvent(event, victim, info));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamageVehicle(VehicleDamageEvent event) {
        MatchPlayer victim = getVictim(event.getVehicle());
        if(victim == null) return;
        processDamageEvent(event, victim.getParticipantState(),
                           damageResolver.resolveDamage(EntityDamageEvent.DamageCause.CUSTOM, event.getVehicle(), event.getAttacker()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onIgnition(EntityCombustByEntityEvent event) {
        MatchPlayer victim = getVictim(event.getEntity());
        if(victim == null) return;
        processDamageEvent(event, victim.getParticipantState(),
                           damageResolver.resolveDamage(EntityDamageEvent.DamageCause.FIRE, event.getEntity(), event.getCombuster()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onIgnition(EntityCombustByBlockEvent event) {
        MatchPlayer victim = getVictim(event.getEntity());
        if(victim == null) return;
        processDamageEvent(event, victim.getParticipantState(),
                           damageResolver.resolveDamage(EntityDamageEvent.DamageCause.FIRE, event.getEntity(), event.getCombuster()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPotionSplash(final PotionSplashEvent event) {
        final ThrownPotion potion = event.getPotion();
        if(PotionClassification.classify(potion) != PotionClassification.HARMFUL) return;

        for(LivingEntity victim : event.getAffectedEntities()) {
            final ParticipantState victimState = getMatch().getParticipantState(victim);
            final DamageInfo damageInfo = damageResolver.resolveDamage(EntityDamageEvent.DamageCause.MAGIC, victim, potion);

            if(victimState != null && queryDamage(event, victimState, damageInfo).isDenied()) {
                event.setIntensity(victim, 0);
            }
        }
    }

    @EventHandler
    public void onPotionLinger(final AreaEffectCloudApplyEvent event) {
        final AreaEffectCloud cloud = event.getEntity();
        if(PotionClassification.classify(cloud) != PotionClassification.HARMFUL) return;

        for(Iterator<LivingEntity> iterator = event.getAffectedEntities().iterator(); iterator.hasNext(); ) {
            final LivingEntity victim = iterator.next();
            final ParticipantState victimState = getMatch().getParticipantState(victim);
            final DamageInfo damageInfo = damageResolver.resolveDamage(EntityDamageEvent.DamageCause.MAGIC, victim, cloud);

            if(victimState != null && queryDamage(event, victimState, damageInfo).isDenied()) {
                iterator.remove();
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTarget(EntityTargetEvent event) {
        if(!(event.getEntity() instanceof ExperienceOrb)) {
            ParticipantState victimState = null;
            if(event.getTarget() instanceof Player) {
                // Don't target allies
                MatchPlayer victim = getVictim(event.getTarget());
                if(victim == null) return;
                victimState = victim.getParticipantState();
            } else if(event.getTarget() != null) {
                // Don't target other mobs owned by allies
                victimState = entityResolver.getOwner(event.getTarget());
            }
            if(victimState == null) return;
            DamageInfo damageInfo = damageResolver.resolveDamage(EntityDamageEvent.DamageCause.ENTITY_ATTACK, event.getTarget(), event.getEntity());
            if(queryHostile(event, victimState, damageInfo).isDenied()) {
                event.setCancelled(true);
            }
        }
    }
}
