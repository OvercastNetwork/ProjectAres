package tc.oc.pgm.mutation.types.kit;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import tc.oc.commons.bukkit.inventory.ArmorType;
import tc.oc.commons.bukkit.inventory.Slot;
import tc.oc.commons.bukkit.item.ItemUtils;
import tc.oc.pgm.kits.FreeItemKit;
import tc.oc.pgm.kits.ItemKitApplicator;
import tc.oc.pgm.kits.SlotItemKit;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.mutation.types.KitMutation;

import java.util.List;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArmorMutation extends KitMutation {

    final static FreeItemKit SWORD = new FreeItemKit(item(Material.DIAMOND_SWORD));

    final static SlotItemKit[] ARMOR = new SlotItemKit[] {
        new SlotItemKit(item(Material.DIAMOND_HELMET),     Slot.Armor.forType(ArmorType.HELMET)),
        new SlotItemKit(item(Material.DIAMOND_CHESTPLATE), Slot.Armor.forType(ArmorType.CHESTPLATE)),
        new SlotItemKit(item(Material.DIAMOND_LEGGINGS),   Slot.Armor.forType(ArmorType.LEGGINGS)),
        new SlotItemKit(item(Material.DIAMOND_BOOTS),      Slot.Armor.forType(ArmorType.BOOTS)),
    };

    final WeakHashMap<MatchPlayer, ItemStack> weapons;

    public ArmorMutation(Match match) {
        super(match, true, ARMOR);
        this.kits.add(SWORD);
        weapons = new WeakHashMap<>();
    }

    @Override
    public void apply(MatchPlayer player) {
        // Find the player's first weapon and store it for later
        List<ItemStack> hotbar = Slot.Hotbar.hotbar()
                .map(slot -> slot.getItem(player.getInventory()))
                .collect(Collectors.toList());
        for(ItemStack item : hotbar) {
            if(item != null && ItemUtils.isWeapon(item)) {
                weapons.put(player, item);
                player.getInventory().remove(item);
                break;
            }
        }
        super.apply(player);
    }

    @Override
    public void remove(MatchPlayer player) {
        super.remove(player);
        // Restore the player's old weapon
        ItemStack weapon = weapons.remove(player);
        if(weapon != null) {
            ItemKitApplicator applicator = new ItemKitApplicator();
            applicator.add(weapon);
            applicator.apply(player);
        }
    }

    @Override
    public Stream<? extends Slot> saved() {
        return Slot.Armor.armor();
    }

    @Override
    public void disable() {
        super.disable();
        weapons.clear();
    }

}
