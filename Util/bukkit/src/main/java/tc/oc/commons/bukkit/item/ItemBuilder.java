package tc.oc.commons.bukkit.item;

import java.util.Collections;
import java.util.UUID;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Skin;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.ItemAttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.Dye;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Wool;
import tc.oc.commons.bukkit.inventory.Slot;
import tc.oc.commons.core.ListUtils;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A nice way to build {@link ItemStack}s
 *
 * TODO: attributes, canPlaceOn, canDestroy, potion effects, etc.
 */
public class ItemBuilder<S extends ItemBuilder<?>> {

    private final ItemStack stack;
    private @Nullable ItemMeta meta;

    public ItemBuilder() {
        this(new ItemStack(Material.AIR));
    }

    public ItemBuilder(ItemStack stack) {
        this.stack = stack;
    }

    // Convenient generic alias for this
    @SuppressWarnings("unchecked")
    protected S self() {
        return (S) this;
    }

    public ItemStack get() {
        if(meta != null) {
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public ItemStack copy() {
        return get().clone();
    }

    protected void createMeta() {
        // Bukkit refuses to create meta for air
        final Material material = stack.getType() == Material.AIR ? Material.STONE
                                                                  : stack.getType();
        if(meta != null) {
            // Ensure existing meta is the correct type
            meta = Bukkit.getItemFactory().asMetaFor(meta, material);
        } else {
            meta = stack.getItemMeta();
            if(meta == null) {
                meta = Bukkit.getItemFactory().getItemMeta(material);
            }
        }

        checkNotNull(meta);
    }

    protected ItemMeta meta() {
        if(meta == null) {
            createMeta();
        }
        return meta;
    }

    protected <T extends ItemMeta> T meta(Class<T> type) {
        final ItemMeta meta = meta();
        if(!type.isInstance(meta)) {
            throw new IllegalArgumentException("Item of type " + stack.getType().name() + " cannot have metadata of type " + type.getName());
        }
        return type.cast(meta);
    }

    public S material(Material material) {
        stack.setType(material);
        if(meta != null) {
            // Convert meta if needed
            createMeta();
        }
        return self();
    }

    public S material(MaterialData material) {
        material(material.getItemType());
        stack.setData(material);
        stack.setDurability(material.getData());
        return self();
    }

    public S amount(int amount) {
        stack.setAmount(amount);
        return self();
    }

    public S durability(int durability) {
        stack.setDurability((short) durability);
        return self();
    }

    public S knockBackRestistance(double amount, EquipmentSlot slot) {
        meta().addAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE,
                new ItemAttributeModifier(slot,
                new AttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE.getName(),
                amount, AttributeModifier.Operation.ADD_NUMBER)));
        return self();
    }

    public S speed(double amount, EquipmentSlot slot) {
        meta().addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED,
                new ItemAttributeModifier(slot,
                new AttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED.getName(),
                amount, AttributeModifier.Operation.ADD_SCALAR)));
        return self();
    }

    public S health(double amount, EquipmentSlot slot) {
        meta().addAttributeModifier(Attribute.GENERIC_MAX_HEALTH,
                new ItemAttributeModifier(slot,
                new AttributeModifier(Attribute.GENERIC_MAX_HEALTH.getName(),
                amount, AttributeModifier.Operation.ADD_NUMBER)));
        return self();
    }

    public S armor(double amount, EquipmentSlot slot) {
        meta().addAttributeModifier(Attribute.GENERIC_ARMOR,
                new ItemAttributeModifier(slot,
                new AttributeModifier(Attribute.GENERIC_ARMOR.getName(),
                amount, AttributeModifier.Operation.ADD_NUMBER)));
        return self();
    }

    public S attackDamage(double amount, EquipmentSlot slot) {
        meta().addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE,
                new ItemAttributeModifier(slot,
                new AttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE.getName(),
                amount, AttributeModifier.Operation.ADD_NUMBER)));
        return self();
    }

    public S attackSpeed(double amount, EquipmentSlot slot) {
        meta().addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED,
                new ItemAttributeModifier(slot,
                new AttributeModifier(Attribute.GENERIC_ATTACK_SPEED.getName(),
                amount, AttributeModifier.Operation.ADD_NUMBER)));
        return self();
    }

    public S name(String name) {
        meta().setDisplayName(name);
        return self();
    }

    /**
     * Append a line of lore
     */
    public S lore(String lore) {
        meta().setLore(meta().hasLore() ? ListUtils.append(meta().getLore(), lore)
                : Collections.singletonList(lore));
        return self();
    }

    public S flags(ItemFlag...flags) {
        meta().addItemFlags(flags);
        return self();
    }

    public S unbreakable(boolean unbreakable) {
        meta().setUnbreakable(unbreakable);
        return self();
    }

    public S enchant(Enchantment enchantment, int level) {
        meta().addEnchant(enchantment, level, true);
        return self();
    }

    public S color(DyeColor color) {
        final Material type = stack.getType();
        switch(type) {
            case INK_SACK:
                stack.setData(new Dye(color));
                break;

            case WOOL:
                stack.setData(new Wool(color));
                break;

            default:
                // Assume a colored block
                // TODO verify this, support other things e.g. banners
                stack.setData(new MaterialData(type, color.getWoolData()));
                break;
        }

        // I'm not clear why Bukkit doesn't do this, but it needs to be done.
        // Of course, if we ever support non-block items with this method,
        // this gets a bit more complicated.
        stack.setDurability(stack.getData().getData());

        return self();
    }

    public S skin(String name, UUID uuid, Skin skin) {
        meta(SkullMeta.class).setOwner(name, uuid, skin);
        return self();
    }

    public S shareable(boolean yes) {
        new BooleanItemTag("prevent-sharing", false).set(stack, !yes);
        return self();
    }

    public S locked(boolean yes) {
        new BooleanItemTag("locked", false).set(stack, !yes);
        return self();
    }
}
