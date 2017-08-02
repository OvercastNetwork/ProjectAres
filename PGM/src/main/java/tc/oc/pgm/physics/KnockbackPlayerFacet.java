package tc.oc.pgm.physics;

import java.util.Optional;
import javax.inject.Inject;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerKnockbackEvent;
import org.bukkit.util.Vector;
import tc.oc.commons.bukkit.event.targeted.TargetedEventHandler;
import tc.oc.pgm.damage.DamageDisplayPlayerFacet;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchPlayerFacet;

public class KnockbackPlayerFacet implements MatchPlayerFacet, Listener {

    private final Optional<KnockbackSettings> knockback;
    private final Match match;
    private final Player victim;

    @Inject KnockbackPlayerFacet(Optional<KnockbackSettings> knockback, Match match, Player victim) {
        this.knockback = knockback;
        this.match = match;
        this.victim = victim;
    }

    @TargetedEventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKnockback(PlayerKnockbackEvent event) {
        if(knockback.isPresent() &&
           victim.equals(event.getPlayer()) &&
           event.getDamager() instanceof LivingEntity) {

            event.setCancelled(true);
        }
    }

    @TargetedEventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMelee(EntityDamageByEntityEvent event) {
        if(knockback.isPresent() &&
           victim.equals(event.getEntity()) &&
           event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK &&
           event.getDamager() instanceof LivingEntity &&
           victim.getNoDamageTicks() <= 0) {

            applyImpulses((LivingEntity) event.getDamager());
        }
    }

    private void applyImpulses(LivingEntity attacker) {
        final KnockbackSettings knockback = this.knockback.get();

        final Vector normal = victim.getLocation().subtract(attacker.getLocation()).toVector();
        if(normal.isZero()) return;
        normal.normalize();

        final Vector victimNormal = knockback.pitchedNormal(normal);
        final Vector attackerNormal = normal.times(-1);

        if(victimNormal.isZero() || attackerNormal.isZero()) return;

        final boolean ground = attacker.isOnGround();
        final double attackSpeed = Math.max(0, attacker.getPredictedVelocity().dot(normal));
        final boolean sprint = ground && attackSpeed > knockback.sprintThreshold;

        victim.applyImpulse(victimNormal.multiply(knockback.power(sprint)), true);
        attacker.applyImpulse(attackerNormal.multiply(knockback.recoil(ground) * attackSpeed), true);

        final MatchPlayer matchAttacker = match.getPlayer(attacker);
        if(matchAttacker != null) {
            matchAttacker.facet(DamageDisplayPlayerFacet.class).showKnockback(victim, sprint);
        }
    }
}
