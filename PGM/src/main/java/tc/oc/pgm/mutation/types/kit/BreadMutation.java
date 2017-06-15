package tc.oc.pgm.mutation.types.kit;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import tc.oc.commons.bukkit.item.ItemBuilder;
import tc.oc.commons.core.random.ImmutableWeightedRandomChooser;
import tc.oc.commons.core.random.WeightedRandomChooser;
import tc.oc.pgm.kits.FreeItemKit;
import tc.oc.pgm.kits.Kit;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.mutation.types.KitMutation;

import java.util.List;

public class BreadMutation extends KitMutation {

    final static private ItemStack potionBread = new ItemBuilder(item(Material.BREAD)).name("Potion Bread").unbreakable(true).get();

    final static ImmutableMap<FreeItemKit, Integer> BREADS_MAP = new ImmutableMap.Builder<FreeItemKit, Integer>()
            .put(new FreeItemKit(new ItemBuilder(item(Material.BREAD)).enchant(Enchantment.FIRE_ASPECT, 1).name("Hot Bread").get()), 20)
            .put(new FreeItemKit(new ItemBuilder(item(Material.BREAD)).enchant(Enchantment.DAMAGE_ALL, 5).name("Sharp Bread").get()), 20)
            .put(new FreeItemKit(new ItemBuilder(item(Material.BREAD)).enchant(Enchantment.KNOCKBACK, 2).name("Bouncy Bread").get()), 20)
            .put(new FreeItemKit(new ItemBuilder(item(Material.BREAD)).knockBackRestistance(1, EquipmentSlot.OFF_HAND).knockBackRestistance(1, EquipmentSlot.HAND).name("Iron Bread").get()), 10)
            .put(new FreeItemKit(new ItemBuilder(item(Material.BREAD)).speed(0.3,EquipmentSlot.OFF_HAND).speed(0.3,EquipmentSlot.HAND).name("Fast Bread").get()), 10)
            .put(new FreeItemKit(new ItemBuilder(item(Material.BREAD)).armor(10,EquipmentSlot.OFF_HAND).armor(10,EquipmentSlot.HAND).name("Armored Bread").get()), 10)
            .put(new FreeItemKit(potionBread), 10)
            .put(new FreeItemKit(new ItemBuilder(item(Material.BREAD)).enchant(Enchantment.DAMAGE_ALL, 10).name("Very Sharp Bread").get()), 6)
            .put(new FreeItemKit(new ItemBuilder(item(Material.BREAD)).speed(1,EquipmentSlot.OFF_HAND).speed(1,EquipmentSlot.HAND).name("Very Fast Bread").get()), 3)
            .put(new FreeItemKit(new ItemBuilder(item(Material.BREAD)).enchant(Enchantment.FIRE_ASPECT, 10).name("Very Hot Bread").get()), 3)
            .put(new FreeItemKit(new ItemBuilder(item(Material.BREAD)).enchant(Enchantment.KNOCKBACK, 10).name("Very Bouncy Bread").get()), 2)
            .put(new FreeItemKit(new ItemBuilder(item(Material.BREAD)).enchant(Enchantment.DAMAGE_ALL, 20).name("Insanely Sharp Bread").get()), 1)
            .put(new FreeItemKit(new ItemBuilder(item(Material.BREAD)).enchant(Enchantment.KNOCKBACK, 100).name("Insanely Bouncy Bread").get()), 1)
            .put(new FreeItemKit(new ItemBuilder(item(Material.BREAD)).enchant(Enchantment.FIRE_ASPECT, 100).name("Insanely Hot Bread").get()), 1)
            .build();

    final static WeightedRandomChooser<PotionEffectType, Integer> POTIONS = new ImmutableWeightedRandomChooser<>(PotionMutation.BAD_MAP);
    final static WeightedRandomChooser<FreeItemKit, Integer> BREADS = new ImmutableWeightedRandomChooser<>(BREADS_MAP);

    final static Range<Integer> AMPLIFIER_RANGE = Range.closed(0, 3);
    final static Range<Integer> DURATION_RANGE = Range.closed(3, 10);

    public BreadMutation(Match match) {
        super(match, false);
    }

    @Override
    public void kits(MatchPlayer player, List<Kit> kits) {
        super.kits(player, kits);
        kits.add(BREADS.choose(entropy()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if(event.getDamager() instanceof Player && ((((Player) event.getDamager()).getItemInHand()).isSimilar(potionBread)
                || (((Player) event.getDamager()).getInventory().getItemInOffHand()).isSimilar(potionBread))) {
            if (event.getEntity() instanceof LivingEntity) {
                ((LivingEntity)event.getEntity()).addPotionEffect(new PotionEffect(POTIONS.choose(entropy()), 20 * entropy().randomInt(DURATION_RANGE), entropy().randomInt(AMPLIFIER_RANGE)));
            }
        }
    }

}
