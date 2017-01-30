package tc.oc.pgm.mutation.submodule;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import java.time.Duration;
import tc.oc.commons.bukkit.inventory.ArmorType;
import tc.oc.commons.bukkit.inventory.Slot;
import tc.oc.commons.bukkit.item.ItemUtils;
import tc.oc.pgm.doublejump.DoubleJumpKit;
import tc.oc.pgm.kits.FreeItemKit;
import tc.oc.pgm.kits.Kit;
import tc.oc.pgm.kits.KitNode;
import tc.oc.pgm.kits.PotionKit;
import tc.oc.pgm.kits.SlotItemKit;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.ParticipantState;

public class MutationModules {

    public static class Explosives extends KitMutationModule {
        public static final Float MULTIPLIER = 1.75f;
        private static final Kit KIT = KitNode.of(new FreeItemKit(new ItemStack(Material.FLINT_AND_STEEL)),
                                                  new FreeItemKit(new ItemStack(Material.TNT, 16)));

        public Explosives(Match match) {
            super(match, false, KIT);
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onExplosionPrime(ExplosionPrimeEvent event) {
            event.setRadius(event.getRadius() * MULTIPLIER);
        }
    }

    public static class Strength extends KitMutationModule {
        public static final PotionEffect EFFECT = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 0, false, false);
        public static final PotionKit KIT = new PotionKit(EFFECT);

        public Strength(Match match) {
            super(match, true, KIT);
        }
    }

    public static class DoubleJump extends KitMutationModule {
        public static final DoubleJumpKit KIT = new DoubleJumpKit(true, 3f, Duration.ofSeconds(5), true);

        public DoubleJump(Match match) {
            super(match, false, KIT);
        }
    }

    public static class Invisibility extends KitMutationModule {
        public static final PotionEffect EFFECT = new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false);
        public static final PotionKit KIT = new PotionKit(EFFECT);

        public Invisibility(Match match) {
            super(match, true, KIT);
        }
    }

    public static class Lightning extends TargetableMutationModule {
        public static final Duration FREQUENCY = Duration.ofSeconds(30);

        public Lightning(Match match) {
            super(match, FREQUENCY, 3);
        }

        @Override
        public void execute(ParticipantState player) {
            match.getWorld().strikeLightning(player.getLocation().clone().add(Vector.getRandom()));
        }
    }

    public static class Rage extends MutationModule {
        public static final Integer DAMAGE = Integer.MAX_VALUE;

        public Rage(Match match) {
            super(match);
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onPlayerDamage(EntityDamageByEntityEvent event) {
            Entity damager = event.getDamager();
            if ((damager instanceof Player && ItemUtils.isWeapon(((Player) damager).getItemInHand())) ||
                (damager instanceof Projectile && ((Projectile) damager).getShooter() instanceof Player)) {
                event.setDamage(DAMAGE);
            }
        }
    }

    public static class Elytra extends KitMutationModule {
        public static final Kit KIT = KitNode.of(new SlotItemKit(new ItemStack(Material.ELYTRA), Slot.Armor.forType(ArmorType.CHESTPLATE)),
                                                 new DoubleJumpKit(true, 6f, Duration.ofSeconds(30), true));

        public Elytra(Match match) {
            super(match, true, KIT);
        }
    }

}
