package tc.oc.pgm.kits;

import java.util.Map;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.registry.Key;
import tc.oc.commons.bukkit.item.ItemUtils;
import tc.oc.commons.bukkit.util.NMSHacks;
import tc.oc.commons.core.inspect.Inspectable;
import tc.oc.commons.core.util.MapUtils;

public interface ItemKit extends Kit {
    ItemStack item();
}

abstract class BaseItemKit extends Kit.Impl implements ItemKit {
    @Inspect private Material material()                    { return item().getType(); }
    @Inspect private int damage()                           { return item().getDurability(); }
    @Inspect private int amount()                           { return item().getAmount(); }
    @Inspect private Optional<ItemMeta> meta()              { return ItemUtils.tryMeta(item()); }
    @Inspect private Map<Key, Integer> enchants()           { return MapUtils.transformKeys(item().getEnchantments(),
                                                                                            enchantment -> NMSHacks.getKey(enchantment)); }
}