package tc.oc.pgm.mutation.types.kit;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import tc.oc.commons.bukkit.item.ItemUtils;
import tc.oc.commons.core.random.ImmutableWeightedRandomChooser;
import tc.oc.commons.core.random.WeightedRandomChooser;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.mutation.types.KitMutation;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class EnchantmentMutation extends KitMutation {

    final static ImmutableMap<Integer, Integer> LEVELS_MAP = new ImmutableMap.Builder<Integer, Integer>()
            .put(1, 25)
            .put(2, 5)
            .put(3, 1)
            .build();

    final static ImmutableMap<Enchantment, Integer> ARMOR_MAP = new ImmutableMap.Builder<Enchantment, Integer>()
            .put(Enchantment.PROTECTION_ENVIRONMENTAL, 15)
            .put(Enchantment.PROTECTION_PROJECTILE,    10)
            .put(Enchantment.PROTECTION_EXPLOSIONS,    5)
            .put(Enchantment.PROTECTION_FIRE,          5)
            .put(Enchantment.THORNS,                   1)
            .build();

    final static ImmutableMap<Enchantment, Integer> BOOTS_MAP = new ImmutableMap.Builder<Enchantment, Integer>()
            .putAll(ARMOR_MAP)
            .put(Enchantment.PROTECTION_FALL, 10)
            .put(Enchantment.DEPTH_STRIDER,   3)
            .put(Enchantment.FROST_WALKER,    1)
            .build();

    final static ImmutableMap<Enchantment, Integer> WEAPONS_MAP = new ImmutableMap.Builder<Enchantment, Integer>()
            .put(Enchantment.DAMAGE_ALL,    15)
            .put(Enchantment.KNOCKBACK,     10)
            .put(Enchantment.MENDING,       5)
            .put(Enchantment.SWEEPING_EDGE, 5)
            .put(Enchantment.FIRE_ASPECT,   1)
            .build();

    final static ImmutableMap<Enchantment, Integer> TOOLS_MAP = new ImmutableMap.Builder<Enchantment, Integer>()
            .put(Enchantment.DIG_SPEED,         10)
            .put(Enchantment.SILK_TOUCH,        5)
            .put(Enchantment.LOOT_BONUS_BLOCKS, 5)
            .put(Enchantment.LOOT_BONUS_MOBS,   5)
            .put(Enchantment.LUCK,              1)
            .build();

    final static ImmutableMap<Enchantment, Integer> BOWS_MAP = new ImmutableMap.Builder<Enchantment, Integer>()
            .put(Enchantment.ARROW_DAMAGE,    10)
            .put(Enchantment.ARROW_KNOCKBACK, 5)
            .put(Enchantment.ARROW_FIRE,      1)
            .build();

    final static Map<Enchantment, Integer> FISHING_MAP = new ImmutableMap.Builder<Enchantment, Integer>()
            .put(Enchantment.KNOCKBACK, 3)
            .put(Enchantment.LURE,      1)
            .build();

    final static WeightedRandomChooser<Integer, Integer> LEVELS = new ImmutableWeightedRandomChooser<>(LEVELS_MAP);
    final static WeightedRandomChooser<Enchantment, Integer> ARMOR = new ImmutableWeightedRandomChooser<>(ARMOR_MAP);
    final static WeightedRandomChooser<Enchantment, Integer> BOOTS = new ImmutableWeightedRandomChooser<>(BOOTS_MAP);
    final static WeightedRandomChooser<Enchantment, Integer> WEAPONS = new ImmutableWeightedRandomChooser<>(WEAPONS_MAP);
    final static WeightedRandomChooser<Enchantment, Integer> TOOLS = new ImmutableWeightedRandomChooser<>(TOOLS_MAP);
    final static WeightedRandomChooser<Enchantment, Integer> BOWS = new ImmutableWeightedRandomChooser<>(BOWS_MAP);
    final static WeightedRandomChooser<Enchantment, Integer> FISHING = new ImmutableWeightedRandomChooser<>(FISHING_MAP);

    Map<Entity, Map<ItemStack, Map<Enchantment, Integer>>> savedEnchantments;

    public EnchantmentMutation(Match match) {
        super(match, true);
        this.savedEnchantments = new WeakHashMap<>();
    }

    public void apply(ItemStack item, EntityEquipment equipment) {
        // Pick the enchantment chooser depending on the item's material
        WeightedRandomChooser<Enchantment, Integer> chooser;
        if(item == null || ItemUtils.isNothing(item)) {
            return;
        } else if(ItemUtils.isWeapon(item)) {
            chooser = WEAPONS;
        } else if(ItemUtils.isArmor(item)) {
            if(equipment.getBoots().equals(item)) {
                chooser = BOOTS;
            } else {
                chooser = ARMOR;
            }
        } else if(ItemUtils.isTool(item)) {
            chooser = TOOLS;
        } else if(Material.FISHING_ROD.equals(item.getType())) {
            chooser = FISHING;
        } else if(Material.BOW.equals(item.getType())) {
            chooser = BOWS;
        } else {
            chooser = null;
        }
        if(chooser != null) {
            // Save the item's enchantments if they need to be restored
            Entity entity = equipment.getHolder();
            Map<ItemStack, Map<Enchantment, Integer>> byEntity = savedEnchantments.getOrDefault(entity, new WeakHashMap<>());
            byEntity.put(item, ImmutableMap.copyOf(item.getEnchantments()));
            savedEnchantments.put(entity, byEntity);
            // Apply the new enchantments
            int amountOfEnchants = LEVELS.choose(entropy());
            for(int i = 0; i < amountOfEnchants; i++) {
                item.addUnsafeEnchantment(chooser.choose(entropy()), LEVELS.choose(entropy()));
            }

        }
    }

    @Override
    public void apply(MatchPlayer player) {
        super.apply(player);
        player.getInventory().forEach(item -> {
            // Random number of enchantments on each item
            int numberOfEnchants = LEVELS.choose(entropy());
            for(int i = 0; i < numberOfEnchants; i++) {
                apply(item, player.getBukkit().getEquipment());
            }
        });
    }

    @Override
    public void remove(MatchPlayer player) {
        super.remove(player);
        remove(player.getBukkit());
    }

    public void remove(Entity entity) {
        savedEnchantments.getOrDefault(entity, new HashMap<>()).forEach((ItemStack item, Map<Enchantment, Integer> old) -> {
            item.getEnchantments().keySet().forEach(item::removeEnchantment); // Clear the current enchantments
            item.addUnsafeEnchantments(old); // Add the old enchantments back
        });
        savedEnchantments.remove(entity);
    }

    @Override
    public void disable() {
        ImmutableSet.copyOf(savedEnchantments.keySet()).forEach(this::remove);
        savedEnchantments.clear();
    }

}
