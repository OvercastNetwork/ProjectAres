package tc.oc.commons.bukkit.util;

import net.minecraft.server.NBTTagCompound;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

import java.util.*;

public class ItemCreator {

    private Material material;
    private String name;
    private int size = 1;
    private int data = 0;
    private List<String> lore = new ArrayList<>();
    private Map<Enchantment, Integer> enchantments = new HashMap<>();
    private Color armorColor;
    private String skullOwner;
    private PotionEffect potionEffect;
    private List<HideFlag> hideFlags;
    private boolean unbreakable;

    public ItemCreator(Material material) {
        setMaterial(material);
        setName(material.toString());
    }

    public Material getMaterial() {
        return material;
    }

    public ItemCreator setMaterial(Material material) {
        this.material = material;
        return this;
    }

    public String getName() {
        return name;
    }

    public ItemCreator setName(String name) {
        this.name = name;
        return this;
    }

    public int getSize() {
        return size;
    }

    public ItemCreator setSize(int size) {
        this.size = size;
        return this;
    }

    public int getData() {
        return data;
    }

    public ItemCreator setData(int data) {
        this.data = data;
        return this;
    }

    public List<String> getLore() {
        return lore;
    }

    public ItemCreator setLore(String... lore) {
        this.lore.clear();
        this.lore.addAll(Arrays.asList(lore));
        return this;
    }

    public ItemCreator addLore(String... lore) {
        this.lore.addAll(Arrays.asList(lore));
        return this;
    }

    public ItemCreator clearLore() {
        this.lore.clear();
        return this;
    }

    public Map<Enchantment, Integer> getEnchantments() {
        return enchantments;
    }

    public ItemCreator addEnchantment(Enchantment enchantment, int level) {
        this.enchantments.put(enchantment, level);
        return this;
    }

    public ItemCreator removeEnchantments() {
        this.enchantments.clear();
        return this;
    }

    public Color getArmorColor() {
        return armorColor;
    }

    public ItemCreator setArmorColor(Color armorColor) {
        this.armorColor = armorColor;
        return this;
    }

    public String getSkullOwner() {
        return skullOwner;
    }

    public ItemCreator setSkullOwner(String owner) {
        this.skullOwner = owner;
        return this;
    }

    public PotionEffect getPotionEffect() {
        return potionEffect;
    }

    public ItemCreator setPotionEffect(PotionEffect potionEffect) {
        this.potionEffect = potionEffect;
        return this;
    }

    public boolean getUnbreakable() {
        return unbreakable;
    }

    public ItemCreator setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    public enum HideFlag {
        ENCHANTMENTS(1),
        ATTRIBUTES(2),
        UNBREAKABLE(4),
        CAN_DESTROY(8),
        CAN_PLACE_ON(16),
        OTHERS(32),
        ALL(ENCHANTMENTS.getValue() +
                ATTRIBUTES.getValue() +
                UNBREAKABLE.getValue() +
                CAN_DESTROY.getValue() +
                CAN_PLACE_ON.getValue() +
                OTHERS.getValue()
        );

        private int value;

        HideFlag(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public List<HideFlag> getHideFlags() {
        return hideFlags;
    }

    @Deprecated
    public ItemCreator setHideFlags(boolean all) {
        this.hideFlags = all ? Collections.singletonList(HideFlag.ALL) : Collections.emptyList();
        return this;
    }

    public ItemCreator setHideFlags(HideFlag... hideFlags) {
        this.hideFlags = Arrays.asList(hideFlags);
        return this;
    }

    public ItemStack create() {
        ItemStack item = new ItemStack(material, size, (short)data);
        if (!item.getType().equals(Material.AIR)) {
            net.minecraft.server.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
            NBTTagCompound tag = new NBTTagCompound();
            if (hideFlags != null && hideFlags.size() > 0) {
                int hideFlagValue = 0;
                for (HideFlag flag : getHideFlags()) {
                    hideFlagValue += flag.getValue();
                }
                hideFlagValue = Math.max(1, Math.min(63, hideFlagValue));
                tag.setInt("HideFlags", hideFlagValue);
            }
            if (skullOwner != null) {
                tag.setString("SkullOwner", skullOwner);
            }
            nmsStack.setTag(tag);
            item = CraftItemStack.asBukkitCopy(nmsStack);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
            item.addUnsafeEnchantments(enchantments);
            if (armorColor != null) {
                LeatherArmorMeta armorMeta = (LeatherArmorMeta) item.getItemMeta();
                armorMeta.setColor(armorColor);
                item.setItemMeta(armorMeta);
            }
            if (potionEffect != null) {
                PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
                potionMeta.setMainEffect(potionEffect.getType());
                potionMeta.addCustomEffect(potionEffect, false);
                item.setItemMeta(potionMeta);
            }
        }
        return item;
    }

}