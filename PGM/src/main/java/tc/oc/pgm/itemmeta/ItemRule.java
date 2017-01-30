package tc.oc.pgm.itemmeta;

import java.util.Set;

import org.bukkit.attribute.ItemAttributeModifier;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import tc.oc.commons.bukkit.item.ItemUtils;
import tc.oc.pgm.utils.MaterialMatcher;

public class ItemRule {
    final MaterialMatcher items;
    final PotionMeta meta;
    final boolean defaultAttributes;

    public ItemRule(MaterialMatcher items, PotionMeta meta, boolean defaultAttributes) {
        this.items = items;
        this.meta = meta;
        this.defaultAttributes = defaultAttributes;
    }

    public boolean matches(ItemStack stack) {
        return items.matches(stack);
    }

    public void apply(ItemStack stack) {
        ItemUtils.addPotionEffects(stack, this.meta.getCustomEffects());

        ItemMeta meta = stack.getItemMeta();
        if(meta != null) {
            if(this.meta.hasDisplayName()) {
                meta.setDisplayName(this.meta.getDisplayName());
            }

            if(this.meta.hasLore()) {
                meta.setLore(this.meta.getLore());
            }

            Set<ItemFlag> flags = this.meta.getItemFlags();
            meta.addItemFlags(flags.toArray(new ItemFlag[flags.size()]));

            ItemUtils.addEnchantments(meta, this.meta.getEnchants());

            for(String attribute : this.meta.getModifiedAttributes()) {
                for(ItemAttributeModifier modifier : this.meta.getAttributeModifiers(attribute)) {
                    meta.addAttributeModifier(attribute, modifier);
                }
            }

            if(this.meta.isUnbreakable()) meta.setUnbreakable(true);
            meta.setCanDestroy(ItemUtils.unionMaterials(meta.getCanDestroy(), this.meta.getCanDestroy()));
            meta.setCanPlaceOn(ItemUtils.unionMaterials(meta.getCanPlaceOn(), this.meta.getCanPlaceOn()));

            stack.setItemMeta(meta);
        }
    }
}
