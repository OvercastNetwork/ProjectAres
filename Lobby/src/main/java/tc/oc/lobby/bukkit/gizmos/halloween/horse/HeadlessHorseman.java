package tc.oc.lobby.bukkit.gizmos.halloween.horse;

import com.google.common.collect.ImmutableMap;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import tc.oc.commons.bukkit.inventory.ArmorType;
import tc.oc.commons.bukkit.inventory.Slot;
import tc.oc.commons.bukkit.item.ItemBuilder;

public class HeadlessHorseman {
    private final static ImmutableMap<Slot, ItemStack> ARMOR_MAP = ImmutableMap.of(
        Slot.Armor.forType(ArmorType.HELMET), new ItemBuilder().material(Material.JACK_O_LANTERN).get(),
        Slot.Armor.forType(ArmorType.CHESTPLATE), new ItemBuilder().material(Material.LEATHER_CHESTPLATE).get(),
        Slot.Armor.forType(ArmorType.LEGGINGS), new ItemBuilder().material(Material.LEATHER_LEGGINGS).get(),
        Slot.Armor.forType(ArmorType.BOOTS), new ItemBuilder().material(Material.LEATHER_BOOTS).get());

    private final static EntityType HORSE_TYPE = EntityType.SKELETON_HORSE;
    private final Player viewer;
    private final HeadlessHorse headlessHorse;
    private static final Color ARMOR_COLOR = Color.fromRGB(84, 5, 40);

    public HeadlessHorseman(Player viewer) {
        this.viewer = viewer;
        this.headlessHorse = new HeadlessHorse(viewer);
        this.mutate();
    }

    private void mutate() {
        headlessHorse.spawn(viewer.getLocation(), (Class<AbstractHorse>) HORSE_TYPE.getEntityClass());
        ARMOR_MAP.forEach(this::colorAndEquip);
        viewer.playSound(viewer.getLocation(), Sound.ENTITY_SKELETON_HORSE_DEATH, 1.25f, 1.25f);
    }

    private void colorAndEquip(Slot slot, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof LeatherArmorMeta) {
            LeatherArmorMeta armorMeta = (LeatherArmorMeta) meta;
            armorMeta.setColor(ARMOR_COLOR);
            item.setItemMeta(meta);
        }
        slot.putItem(viewer.getInventory(), item);
    }

    public void restore() {
        headlessHorse.despawn();
        viewer.getInventory().armor().clear();
        viewer.getInventory().setChestplate(new ItemStack(Material.ELYTRA));
        viewer.playSound(viewer.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1f, 1f);
    }

    public HeadlessHorse getHeadlessHorse() {
        return headlessHorse;
    }
}