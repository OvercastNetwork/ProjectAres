package tc.oc.pgm.damage;

import javax.inject.Inject;

import me.anxuiz.settings.SettingManager;
import me.anxuiz.settings.bukkit.PlayerSettings;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.util.Vector;
import java.time.Duration;
import tc.oc.commons.bukkit.event.targeted.TargetedEventHandler;
import tc.oc.commons.bukkit.util.NMSHacks;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.scheduler.Task;
import tc.oc.commons.core.util.TimeUtils;
import tc.oc.pgm.events.MatchPlayerDamageEvent;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayerFacet;
import tc.oc.pgm.match.MatchScheduler;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.tracker.damage.MeleeInfo;

public class DamageDisplayPlayerFacet implements MatchPlayerFacet, Listener {

    private static final Vector DELTA = new Vector(0, 0.02, 0);
    private static final Duration DURATION = Duration.ofMillis(750);

    private final Match match;
    private final Player attacker;
    private final SettingManager settings;

    @Inject DamageDisplayPlayerFacet(Match match, Player attacker) {
        this.match = match;
        this.attacker = attacker;
        this.settings = PlayerSettings.getManager(attacker);
    }

    private Location hitLocation(LivingEntity victim) {
        // Move away from the victim a bit, but not too close to the attacker
        return HitboxPlayerFacet.meleeHitLocation(match, victim, attacker, 1, -0.2);
    }

    public void showKnockback(LivingEntity victim, boolean sprint) {
        if(!sprint) return;
        if(!settings.getValue(DamageSettings.KNOCKBACK_PARTICLES, Boolean.class)) return;
        attacker.spawnParticle(Particle.EXPLOSION_NORMAL, hitLocation(victim), 3, 0, 0, 0, 0.5);
    }

    public void showDamage(LivingEntity victim, double damage) {
        if(!settings.getValue(DamageSettings.DAMAGE_NUMBERS, Boolean.class)) return;

        // Calculate the location of the hit on the victim.
        final Location hit = hitLocation(victim).subtract(0, 0.5, 0); // Shift armor stand down a bit so nametag is centered

        final NMSHacks.FakeEntity entity = new NMSHacks.FakeArmorStand(
            attacker.getWorld(),
            ChatColor.RED.toString() + ChatColor.BOLD + ((int) (damage * 5))
        );
        entity.spawn(attacker, hit);

        final MatchScheduler scheduler = match.getScheduler(MatchScope.LOADED);
        final Task[] task = new Task[1]; // HACK - need a better way for a task to cancel itself
        task[0] = scheduler.createRepeatingTask(1, new Runnable() {
            long ticks = TimeUtils.toTicks(DURATION);

            void cancel() {
                task[0].cancel();
            }

            @Override
            public void run() {
                if(!attacker.isOnline()) cancel();

                if(--ticks <= 0) {
                    entity.destroy(attacker);
                    cancel();
                }

                entity.move(attacker, DELTA, false);
            }
        });
    }

    @TargetedEventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(MatchPlayerDamageEvent event) {
        if(!event.isAttacker(attacker)) return;
        if(!(event.info() instanceof MeleeInfo)) return;
        // Absorbed damage is removed from the final value, so we add it back
        showDamage(event.victim().getBukkit(), event.cause().getFinalDamage() - event.cause().getDamage(EntityDamageEvent.DamageModifier.ABSORPTION));
    }

    @TargetedEventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSwing(PlayerAnimationEvent event) {
        if(event.getAnimationType() != PlayerAnimationType.ARM_SWING) return;
        if(event.getPlayer().isDigging()) return;
        updateSpeedometer();
    }

    private void updateSpeedometer() {
        if(!settings.getValue(DamageSettings.ATTACK_SPEEDOMETER, Boolean.class)) return;

        final float damage = attacker.getAttackCooldownCoefficient();
        final ChatColor damageColor;
        if(damage < 0.5) {
            damageColor = ChatColor.RED;
        } else if(damage < 0.75) {
            damageColor = ChatColor.GOLD;
        } else if(damage < 1) {
            damageColor = ChatColor.YELLOW;
        } else {
            damageColor = ChatColor.GREEN;
        }

        final double cps = 20.0 / (1 + attacker.getAttackCooldownTicks());
        final String cpsText = Double.isInfinite(cps) ? "\u221e"
                                                      : String.format("%.1f", cps);
        attacker.sendMessage(
            ChatMessageType.ACTION_BAR,
            new Component(ChatColor.WHITE)
                .extra("Damage: ")
                .extra(new Component(((int) (attacker.getAttackCooldownCoefficient() * 100D)) + "%", damageColor, ChatColor.BOLD))
                .extra("   CPS: ")
                .extra(new Component(cpsText, ChatColor.AQUA, ChatColor.BOLD))
                .extra("+")
        );
    }
}
