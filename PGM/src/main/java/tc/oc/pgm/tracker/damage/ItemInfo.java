package tc.oc.pgm.tracker.damage;

import javax.annotation.Nullable;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Components;
import tc.oc.commons.bukkit.util.NMSHacks;
import tc.oc.pgm.match.ParticipantState;

public class ItemInfo extends OwnerInfoBase implements PhysicalInfo {

    private static final ItemStack AIR_STACK = new ItemStack(Material.AIR);

    @Inspect private final ItemStack item;

    public ItemInfo(@Nullable ItemStack item, @Nullable ParticipantState owner) {
        super(owner);
        this.item = item != null ? item : AIR_STACK;
    }

    public ItemInfo(@Nullable ItemStack item) {
        this(item, null);
    }

    public ItemStack getItem() {
        return item;
    }

    public boolean isEnchanted() {
        return !getItem().getEnchantments().isEmpty();
    }

    @Override
    public String getIdentifier() {
        return getItem().getType().name();
    }

    @Override
    public BaseComponent getLocalizedName() {
        if(getItem().hasItemMeta()) {
            String customName = getItem().getItemMeta().getDisplayName();
            if(customName != null) {
                return Components.fromLegacyText(customName);
            }
        }

        String key = NMSHacks.getTranslationKey(getItem());
        return key != null ? new TranslatableComponent(key)
                           : new Component(getItem().getType().name());
    }
}
