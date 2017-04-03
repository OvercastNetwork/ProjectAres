package tc.oc.pgm.mutation.types.kit;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import tc.oc.commons.core.random.ImmutableWeightedRandomChooser;
import tc.oc.commons.core.random.WeightedRandomChooser;
import tc.oc.pgm.kits.FreeItemKit;
import tc.oc.pgm.kits.Kit;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.mutation.types.KitMutation;

import java.util.List;

public class PotionMutation extends KitMutation {

    final static ImmutableMap<PotionEffectType, Integer> BAD_MAP = new ImmutableMap.Builder<PotionEffectType, Integer>()
            .put(PotionEffectType.WEAKNESS,     15)
            .put(PotionEffectType.SLOW,         10)
            .put(PotionEffectType.POISON,       10)
            .put(PotionEffectType.BLINDNESS,    3)
            .put(PotionEffectType.LEVITATION,   1)
            .build();

    final static ImmutableMap<PotionEffectType, Integer> GOOD_MAP = new ImmutableMap.Builder<PotionEffectType, Integer>()
            .put(PotionEffectType.SPEED,             15)
            .put(PotionEffectType.INCREASE_DAMAGE,   15)
            .put(PotionEffectType.DAMAGE_RESISTANCE, 10)
            .put(PotionEffectType.REGENERATION,      10)
            .put(PotionEffectType.FIRE_RESISTANCE,   10)
            .put(PotionEffectType.HEALTH_BOOST,      5)
            .put(PotionEffectType.JUMP,              5)
            .put(PotionEffectType.INVISIBILITY,      1)
            .build();

    final static ImmutableMap<Material, Integer> BOTTLE_BAD_MAP = new ImmutableMap.Builder<Material, Integer>()
            .put(Material.SPLASH_POTION,    5)
            .put(Material.LINGERING_POTION, 1)
            .build();

    final static ImmutableMap<Material, Integer> BOTTLE_GOOD_MAP = new ImmutableMap.Builder<Material, Integer>()
            .putAll(BOTTLE_BAD_MAP)
            .put(Material.POTION, 10)
            .build();


    final static WeightedRandomChooser<PotionEffectType, Integer> BAD = new ImmutableWeightedRandomChooser<>(BAD_MAP);
    final static WeightedRandomChooser<PotionEffectType, Integer> GOOD = new ImmutableWeightedRandomChooser<>(GOOD_MAP);

    final static WeightedRandomChooser<Material, Integer> BAD_BOTTLE = new ImmutableWeightedRandomChooser<>(BOTTLE_BAD_MAP);
    final static WeightedRandomChooser<Material, Integer> GOOD_BOTTLE = new ImmutableWeightedRandomChooser<>(BOTTLE_GOOD_MAP);

    final static Range<Integer> BAD_DURATION_RANGE = Range.closed(3, 10);
    final static Range<Integer> GOOD_DURATION_RANGE = Range.closed(10, 45);

    final static Range<Integer> AMOUNT_RANGE = Range.closed(1, 3);
    final static Range<Integer> AMPLIFIER_RANGE = Range.closed(0, 2);

    public PotionMutation(Match match) {
        super(match, false);
    }

    @Override
    public void kits(MatchPlayer player, List<Kit> kits) {
        super.kits(player, kits);
        int numberOfPotions = entropy().randomInt(AMOUNT_RANGE);
        for(int i = 0; i < numberOfPotions; i++) {
            WeightedRandomChooser<PotionEffectType, Integer> type;
            WeightedRandomChooser<Material, Integer> material;
            Range<Integer> range;
            // Determine whether the potion will be "good" or "bad"
            if(random().nextBoolean()) {
                type = BAD;
                material = BAD_BOTTLE;
                range = BAD_DURATION_RANGE;
            } else {
                type = GOOD;
                material = GOOD_BOTTLE;
                range = GOOD_DURATION_RANGE;
            }
            // Choose all the random attributes
            PotionEffectType effect = type.choose(entropy());
            Material bottle = material.choose(entropy());
            int duration = 20 * entropy().randomInt(range);
            int amplifier = entropy().randomInt(AMPLIFIER_RANGE);
            // Apply the attributes to the item stack
            ItemStack potion = item(bottle);
            PotionMeta meta = (PotionMeta) potion.getItemMeta();
            meta.addCustomEffect(new PotionEffect(effect, duration, amplifier), true);
            potion.setItemMeta(meta);
            kits.add(new FreeItemKit(potion));
        }
    }

}
