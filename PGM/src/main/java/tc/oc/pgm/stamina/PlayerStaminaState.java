package tc.oc.pgm.stamina;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.Sets;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import tc.oc.commons.bukkit.item.ItemUtils;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.util.Numbers;
import tc.oc.commons.core.util.DefaultMapAdapter;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.commons.bukkit.event.BlockPunchEvent;
import tc.oc.pgm.stamina.mutators.StaminaMutator;
import tc.oc.pgm.stamina.symptoms.PotionSymptom;
import tc.oc.pgm.stamina.symptoms.StaminaSymptom;

public class PlayerStaminaState {

    final StaminaOptions options;
    final MatchPlayer player;

    double stamina;             // player's stamina (0..1)
    boolean moving;             // moved during current tick
    boolean onGround = true;    // was on the ground for last movement

    static final long SWING_TICK_WINDOW = 2;
    boolean swinging;           // swung weapon within the last SWING_TICK_WINDOW
    long lastSwingTick;         // world tick time of last weapon swing
    
    static final long SPRINT_TICK_WINDOW = 20;
    long lastSprintTick;

    // Map of total amount of stamina consumed by each mutator (see explanation in #mutateStamina)
    final Map<StaminaMutator, Double> depletionCauses = new DefaultMapAdapter<>(0d);

    // last potion effect levels applied to the player from stamina symptoms (zero-based)
    Map<PotionEffectType, Integer> lastPotionLevels = new DefaultMapAdapter<>(-1);

    PlayerStaminaState(StaminaOptions options, MatchPlayer player) {
        this.options = options;
        this.player = player;
        mutateStamina(null, 1d);
    }

    Iterable<StaminaSymptom> getActiveSymptoms() {
        return Iterables.filter(options.symptoms, new Predicate<StaminaSymptom>() {
            @Override
            public boolean apply(StaminaSymptom symptom) {
                return symptom.range.contains(stamina);
            }
        });
    }

    void mutateStamina(@Nullable StaminaMutator mutator, double newStamina) {
        newStamina = Numbers.clamp(newStamina, 0, 1);
        if(stamina == newStamina) return;

        double oldStamina = stamina;
        stamina = newStamina;

        if(newStamina < oldStamina) {
            if(mutator != null) {
                // If stamina went down, add the loss to the total for the causing mutator.
                depletionCauses.put(mutator, depletionCauses.get(mutator) + oldStamina - newStamina);
            }
        } else {
            // If stamina went up, reduce all depletion causes proportionally.
            for(StaminaMutator cause : ImmutableSet.copyOf(depletionCauses.keySet())) {
                depletionCauses.put(cause, depletionCauses.get(cause) / (1 - oldStamina) * (1 - newStamina));
            }
        }

        applySymptoms(oldStamina);
        refreshPotions();
        refreshMeter();
    }

    void mutateStamina(StaminaMutator mutator) {
        mutateStamina(mutator, mutator.getNumericModifier().credit(stamina));
    }

    void mutateStaminaTick(StaminaMutator mutator) {
        mutateStamina(mutator, mutator.getNumericModifier().credit(stamina, 1d / 20d));
    }

    void applySymptoms(double oldStamina) {
        for(StaminaSymptom symptom : options.symptoms) {
            if(!symptom.range.contains(oldStamina) && symptom.range.contains(stamina)) {
                symptom.apply(player);
            } else if(symptom.range.contains(oldStamina) && !symptom.range.contains(stamina)) {
                symptom.remove(player);
            }
        }
    }

    // Number of bars
    private static final int METER_SCALE = 88;
    private static final RangeMap<Double, ChatColor> METER_COLORS = ImmutableRangeMap.<Double, ChatColor>builder()
        .put(Range.closedOpen(0.2, 0.3), ChatColor.BLUE)
        .put(Range.closedOpen(0.3, 0.5), ChatColor.DARK_AQUA)
        .put(Range.closedOpen(0.5, 0.7), ChatColor.AQUA)
        .put(Range.closedOpen(0.7, 1.0), ChatColor.WHITE)
        .put(Range.singleton(1.0), ChatColor.YELLOW)
        .build();

    private static final BaseComponent SPACE = new Component(" ");

    void refreshMeter() {
        BaseComponent label;
        ChatColor color = METER_COLORS.get(stamina);

        if(color != null) {
            int segments = METER_COLORS.asMapOfRanges().size();
            Deque<BaseComponent> parts = new ArrayDeque<>(segments * 2 + 3);

            parts.add(SPACE);
            parts.add(new TranslatableComponent("stamina.label"));
            parts.add(SPACE);

            for(Map.Entry<Range<Double>, ChatColor> entry : METER_COLORS.asMapOfRanges().entrySet()) {
                int length = (int) (METER_SCALE * (Numbers.clamp(stamina, entry.getKey()) - entry.getKey().lowerEndpoint()));
                Component segment = new Component(Strings.repeat("\u23d0", length), entry.getValue());
                parts.addFirst(segment);
                parts.addLast(segment);
            }

            label = new Component(color).extra(parts);
        } else {
            StaminaMutator cause = getDepletionCause();
            if(cause != null) {
                label = new Component(new TranslatableComponent("stamina.depletedFromMutator",
                                                                new Component(cause.getDescription(), ChatColor.AQUA)),
                                      ChatColor.RED);
            } else {
                label = new Component(new TranslatableComponent("stamina.depleted"), ChatColor.RED);
            }
        }

        player.sendHotbarMessage(label);
    }

    @Nullable StaminaMutator getDepletionCause() {
        StaminaMutator cause = null;
        double max = 0;
        for(Map.Entry<StaminaMutator, Double> entry : depletionCauses.entrySet()) {
            if(entry.getValue() > max) {
                max = entry.getValue();
                cause = entry.getKey();
            }
        }
        return cause;
    }

    void refreshPotions() {
        // Get the maximum level of every potion type caused by current symptoms.
        // It would be nice if PotionSymptom could do this itself, but there doesn't
        // seem to be an easy way.
        Map<PotionEffectType, Integer> newPotionLevels = new DefaultMapAdapter<>(-1);
        for(StaminaSymptom symptom : getActiveSymptoms()) {
            if(symptom instanceof PotionSymptom) {
                PotionSymptom potionSymptom = (PotionSymptom) symptom;
                int maxLevel = newPotionLevels.get(potionSymptom.effect);
                if(maxLevel < potionSymptom.amplifier) {
                    newPotionLevels.put(potionSymptom.effect, potionSymptom.amplifier);
                }
            }
        }

        // Sync the client's effects
        for(PotionEffectType effect : ImmutableSet.copyOf(Sets.union(lastPotionLevels.keySet(), newPotionLevels.keySet()))) {
            int newLevel = newPotionLevels.get(effect);
            if(newLevel != lastPotionLevels.get(effect)) {
                if(newLevel > -1) {
                    lastPotionLevels.put(effect, newLevel);
                    player.getBukkit().addPotionEffect(new PotionEffect(effect, Integer.MAX_VALUE, newLevel, true), true);
                } else {
                    // We remove an effect by setting its time to 2 seconds. This provides a bit of
                    // hysteresis, and also avoids a bug that causes a nether portal to appear when
                    // a nausea potion is removed suddenly.
                    int oldLevel = lastPotionLevels.remove(effect);
                    player.getBukkit().addPotionEffect(new PotionEffect(effect, 40, oldLevel, true), true);
                }
            }
        }
    }

    void tick() {
        long now = player.getMatch().getClock().now().tick;

        if(player.getBukkit().isSprinting()) {
            lastSprintTick = now;
        }
        
        // To detect movement, we just set the moving flag on every move event,
        // and check and clear it here every tick.
        if(moving) {
            moving = false;
            if(lastSprintTick + SPRINT_TICK_WINDOW > now) {
                mutateStaminaTick(options.mutators.run);
            } else if(player.getBukkit().isSneaking()) {
                mutateStaminaTick(options.mutators.sneak);
            } else {
                mutateStaminaTick(options.mutators.walk);
            }
        } else {
            mutateStaminaTick(options.mutators.stand);
        }

        // If a swing was not explained by some other event within the time window,
        // assume it was a swing at the air.
        if(swinging && lastSwingTick + SWING_TICK_WINDOW <= now) {
            swinging = false;
            mutateStamina(options.mutators.meleeMiss);
        }

        refreshMeter();
    }

    void onEvent(PlayerMoveEvent event) {
        moving = true;

        if(onGround && !player.getBukkit().isOnGround() && event.getFrom().getY() < event.getTo().getY()) {
            if(player.getBukkit().isSprinting()) {
                mutateStamina(options.mutators.runJump);
            } else {
                mutateStamina(options.mutators.jump);
            }
        }

        onGround = event.getPlayer().isOnGround();
    }

    boolean isHoldingWeapon() {
        ItemStack holding = player.getBukkit().getItemInHand();
        return holding != null && (ItemUtils.isWeapon(holding) ||
                                   holding.getEnchantmentLevel(Enchantment.DAMAGE_ALL) > 0);
    }

    void onEvent(PlayerAnimationEvent event) {
        if(event.getAnimationType() != PlayerAnimationType.ARM_SWING) return;
        if(event.getPlayer().isDigging()) return;
        if(!isHoldingWeapon()) return;

        swinging = true;
        lastSwingTick = player.getMatch().getClock().now().tick;
    }

    void onEvent(BlockPunchEvent event) {
        swinging = false;
    }

    void onEvent(BlockDamageEvent event) {
        swinging = false;
    }

    void onEvent(BlockBreakEvent event) {
        swinging = false;
    }

    void onEvent(EntityDamageEvent event) {
        if(event.getEntity() == player.getBukkit()) {
            // Player took damage
            mutateStamina(options.mutators.injury);

        } else if(event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK &&
                  event instanceof EntityDamageByEntityEvent &&
                  ((EntityDamageByEntityEvent) event).getDamager() == player.getBukkit()) {

            // Player is damager and attack is melee
            swinging = false;

            for(StaminaSymptom symptom : getActiveSymptoms()) {
                symptom.onAttack(event);
            }

            mutateStamina(options.mutators.meleeHit);
        }
    }

    void onEvent(ProjectileLaunchEvent event) {
        for(StaminaSymptom symptom : getActiveSymptoms()) {
            symptom.onShoot(event);
        }

        mutateStamina(options.mutators.archery);
    }
}
