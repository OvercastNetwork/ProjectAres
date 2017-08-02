package tc.oc.commons.bukkit.item;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Consumer;
import org.bukkit.util.ImmutableMaterialSet;

public class ItemUtils {
    private ItemUtils() {}

    public static void addEnchantments(ItemMeta meta, Map<Enchantment, Integer> enchantments) {
        for(Map.Entry<Enchantment, Integer> enchantment : enchantments.entrySet()) {
            if(meta.getEnchantLevel(enchantment.getKey()) < enchantment.getValue()) {
                meta.addEnchant(enchantment.getKey(), enchantment.getValue(), true);
            }
        }
    }

    public static void addPotionEffects(ItemStack stack, List<PotionEffect> newEffects) {
        if(stack.getType() == Material.POTION && !newEffects.isEmpty()) {
            PotionMeta meta = (PotionMeta) stack.getItemMeta();

            Set<PotionEffect> defaultEffects = new HashSet<>(Potion.fromItemStack(stack).getEffects());
            Collection<PotionEffect> existingEffects;

            if(meta.hasCustomEffects()) {
                existingEffects = meta.getCustomEffects();
            } else {
                existingEffects = defaultEffects;
            }

            Map<PotionEffectType, PotionEffect> effectsByType = new HashMap<>();
            for(PotionEffect effect : existingEffects) {
                effectsByType.put(effect.getType(), effect);
            }

            for(PotionEffect newEffect : newEffects) {
                PotionEffect oldEffect = effectsByType.get(newEffect.getType());
                if(oldEffect == null ||
                   oldEffect.getAmplifier() < newEffect.getAmplifier() ||
                   (oldEffect.getAmplifier() == newEffect.getAmplifier() && oldEffect.getDuration() < newEffect.getDuration())) {

                    effectsByType.put(newEffect.getType(), newEffect);
                }
            }

            if(defaultEffects.equals(ImmutableSet.copyOf(effectsByType.values()))) {
                meta.clearCustomEffects();
            } else {
                for(PotionEffect effect : effectsByType.values()) {
                    meta.addCustomEffect(effect, true);
                }
            }

            stack.setItemMeta(meta);
        }
    }

    public static ImmutableMaterialSet unionMaterials(ImmutableMaterialSet a, ImmutableMaterialSet b) {
        if(a.containsAll(b)) return a;
        if(b.containsAll(a)) return b;
        return ImmutableMaterialSet.of(Sets.union(a, b));
    }

    public static int amount(@Nullable ItemStack stack) {
        return stack == null || stack.getType() == Material.AIR ? 0 : stack.getAmount();
    }

    public static int addAmount(ItemStack stack, int delta) {
        final int amount = stack.getAmount() + delta;
        stack.setAmount(amount);
        return amount;
    }

    public static boolean isNothing(@Nullable ItemStack stack) {
        return amount(stack) == 0;
    }

    public static Optional<ItemStack> something(@Nullable ItemStack stack) {
        return isNothing(stack) ? Optional.empty() : Optional.of(stack);
    }

    private static final String[] TOOLS = {"axe", "hoe", "spade"};

    public static boolean isTool(ItemStack stack) {
        return isTool(stack.getData());
    }

    public static boolean isTool(MaterialData item) {
        return Stream.of(TOOLS).anyMatch(query -> item.getItemType().name().toLowerCase().contains(query));
    }

    public static boolean isArmor(ItemStack stack) {
        return isArmor(stack.getData());
    }

    public static boolean isArmor(MaterialData item) {
        return !Bukkit.getItemFactory().getAttributeModifiers(item, Attribute.GENERIC_ARMOR).isEmpty();
    }

    public static boolean isWeapon(ItemStack stack) {
        return isWeapon(stack.getData());
    }

    public static boolean isWeapon(MaterialData item) {
        return !Bukkit.getItemFactory().getAttributeModifiers(item, Attribute.GENERIC_ATTACK_DAMAGE).isEmpty();
    }

    public static Optional<ItemMeta> tryMeta(ItemStack item) {
        return item.hasItemMeta() ? Optional.of(item.getItemMeta())
                                  : Optional.empty();
    }

    public static void updateMeta(ItemStack item, Consumer<ItemMeta> mutator) {
        final ItemMeta meta = item.getItemMeta();
        mutator.accept(meta);
        item.setItemMeta(meta);
    }

    public static void updateMetaIfPresent(@Nullable ItemStack item, Consumer<ItemMeta> mutator) {
        if(item != null && item.hasItemMeta()) {
            updateMeta(item, mutator);
        }
    }
}
