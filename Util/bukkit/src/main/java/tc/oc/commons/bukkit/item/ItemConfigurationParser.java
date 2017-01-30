package tc.oc.commons.bukkit.item;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.Skin;
import org.bukkit.SkullType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import tc.oc.commons.bukkit.configuration.ConfigUtils;
import tc.oc.commons.bukkit.inventory.Slot;
import tc.oc.commons.bukkit.util.NMSHacks;
import tc.oc.commons.core.util.MapUtils;
import tc.oc.minecraft.api.configuration.InvalidConfigurationException;

import static tc.oc.commons.core.exception.LambdaExceptionUtils.rethrowSupplier;

/**
 * Parses {@link ItemStack}s and {@link Skin}s from {@link ConfigurationSection}s
 */
public class ItemConfigurationParser {

    private final ConfigurationSection root;
    private final Map<String, Skin> skins = new HashMap<>();

    public ItemConfigurationParser(ConfigurationSection root) {
        this.root = root;
    }

    public Skin needSkin(String text) {
        return MapUtils.computeIfAbsent(skins, text, t -> {
            final ConfigurationSection skinSection = root.getConfigurationSection("skins");
            if(skinSection != null) {
                final String referenced = skinSection.getString(text);
                if(referenced != null && !referenced.equals(text)) {
                    return needSkin(referenced);
                }
            }
            return new Skin(text, null);
        });
    }

    public <T extends Slot> T needSlotByPosition(ConfigurationSection section, @Nullable String key, @Nullable String value, Class<T> type) throws InvalidConfigurationException {
        final String[] coords = ConfigUtils.needStringOrSectionName(section, key, value).split("\\s*,\\s*");
        if(coords.length != 2) {
            throw new InvalidConfigurationException(section, key, "Expected slot in \"column, row\" format");
        }

        final int column, row;
        final T slot;
        try {
            slot = Slot.atPosition(type, column = Integer.parseInt(coords[0]), row = Integer.parseInt(coords[1]));
        } catch(NumberFormatException e) {
            throw new InvalidConfigurationException(section, key, e.getMessage());
        }
        if(slot == null) {
            throw new InvalidConfigurationException(section, key, "No slot at column " + column + ", row " + row);
        }

        return slot;
    }

    public Material needItemType(ConfigurationSection section, String key) throws InvalidConfigurationException {
        final Material item = NMSHacks.materialByKey(section.needString(key));
        if(item == null) {
            throw new InvalidConfigurationException(section, key, "Unknown item type '" + key + "'");
        }
        return item;
    }

    public void needSkull(ItemStack stack, ConfigurationSection section, String key) throws InvalidConfigurationException {
        final ItemMeta meta = stack.getItemMeta();
        if(!(meta instanceof SkullMeta)) {
            throw new InvalidConfigurationException(section, key, "Item type " + NMSHacks.getKey(stack.getType()) + " cannot be skinned");
        }
        ((SkullMeta) meta).setOwner("SkullOwner", UUID.randomUUID(), needSkin(section.needString(key)));
        stack.setItemMeta(meta);
    }

    public ItemStack needSkull(ConfigurationSection section, String key) throws InvalidConfigurationException {
        final ItemStack stack = new ItemStack(Material.SKULL_ITEM);
        stack.setDurability((short) SkullType.PLAYER.ordinal());
        needSkull(stack, section, key);
        return stack;
    }

    public ItemStack getItem(ConfigurationSection section, String key, Supplier<ItemStack> def) throws InvalidConfigurationException {
        if(section.isString(key)) {
            return new ItemStack(needItemType(section, key));
        }

        if(!section.isConfigurationSection(key)) {
            return def.get();
        }

        final ConfigurationSection itemSection = section.needSection(key);

        if(itemSection.isString("skull")) {
            return needSkull(itemSection, "skull");
        }

        final Material material = needItemType(itemSection, "id");

        final int damage = itemSection.getInt("damage", 0);
        if(damage < Short.MIN_VALUE || damage > Short.MAX_VALUE) {
            throw new InvalidConfigurationException(itemSection, "damage", "Item damage out of range");
        }

        final ItemStack stack = new ItemStack(material, 1, (short) damage);

        if(itemSection.isString("skin")) {
            needSkull(stack, itemSection, "skin");
        }

        return stack;
    }

    public ItemStack needItem(ConfigurationSection section, String key) throws InvalidConfigurationException {
        return getItem(section, key, rethrowSupplier(() -> {
            throw new InvalidConfigurationException("Missing required item " + key);
        }));
    }
}
