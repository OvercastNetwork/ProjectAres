package tc.oc.pgm.mutation.types.kit;

import com.google.common.collect.Range;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TippedArrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import tc.oc.commons.core.random.ImmutableWeightedRandomChooser;
import tc.oc.commons.core.random.WeightedRandomChooser;
import tc.oc.pgm.PGM;
import tc.oc.pgm.killreward.KillReward;
import tc.oc.pgm.kits.FreeItemKit;
import tc.oc.pgm.kits.ItemKit;
import tc.oc.pgm.kits.Kit;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.mutation.types.KitMutation;

import java.util.List;

public class ProjectileMutation extends KitMutation {

    final static WeightedRandomChooser<Enchantment, Integer> ENCHANTMENTS = new ImmutableWeightedRandomChooser<>(EnchantmentMutation.BOWS_MAP);
    final static WeightedRandomChooser<PotionEffectType, Integer> POTIONS = new ImmutableWeightedRandomChooser<>(PotionMutation.BAD_MAP);

    final static Range<Integer> ENCHANT_RANGE = Range.closed(1, 3);
    final static Range<Integer> AMPLIFIER_RANGE = Range.closed(0, 3);
    final static Range<Integer> DURATION_RANGE = Range.closed(3, 10);

    final static ItemKit BOW = new FreeItemKit(item(Material.BOW));
    final static ItemKit ARROWS = new FreeItemKit(item(Material.ARROW, 16));

    final static String KEY = "is_modified_arrow";

    public ProjectileMutation(Match match) {
        super(match, false);
        this.rewards.add(new KillReward(ARROWS));
    }

    @Override
    public void apply(MatchPlayer player) {
        super.apply(player);
        Inventory inventory = player.getInventory();
        inventory.all(Material.BOW).values().forEach(arrow -> arrow.addUnsafeEnchantment(ENCHANTMENTS.choose(entropy), entropy.randomInt(ENCHANT_RANGE)));
    }

    @Override
    public void kits(MatchPlayer player, List<Kit> kits) {
        super.kits(player, kits);
        Inventory inventory = player.getInventory();
        if(!inventory.contains(Material.BOW)) kits.add(BOW);
        if(!inventory.contains(Material.ARROW)) kits.add(ARROWS);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBowShoot(EntityShootBowEvent event) {
        Entity projectile = event.getProjectile();
        if(projectile instanceof Arrow && (!projectile.hasMetadata(KEY) || !projectile.getMetadata(KEY, PGM.get()).asBoolean())) {
            Arrow arrow = (Arrow) projectile;
            TippedArrow tipped = world.spawn(projectile.getLocation(), TippedArrow.class);
            tipped.setMetadata(KEY, new FixedMetadataValue(PGM.get(), true));
            tipped.setCritical(arrow.isCritical());
            tipped.setKnockbackStrength(arrow.getKnockbackStrength());
            tipped.setDamage(arrow.getDamage());
            tipped.setShooter(arrow.getShooter());
            tipped.setVelocity(projectile.getVelocity());
            tipped.setPickupRule(Arrow.PickupRule.DISALLOWED);
            tipped.addCustomEffect(new PotionEffect(POTIONS.choose(entropy), 20 * entropy.randomInt(DURATION_RANGE), entropy.randomInt(AMPLIFIER_RANGE)), true);
            arrow.remove();
            event.setCancelled(true);
            match.callEvent(new ProjectileLaunchEvent(tipped));
        }
    }

}
