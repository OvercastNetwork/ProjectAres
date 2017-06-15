package tc.oc.pgm.mutation.types.kit;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import tc.oc.commons.bukkit.inventory.Slot;
import tc.oc.commons.bukkit.item.ItemBuilder;
import tc.oc.commons.bukkit.item.ItemUtils;
import tc.oc.pgm.kits.FreeItemKit;
import tc.oc.pgm.kits.ItemKitApplicator;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchState;
import tc.oc.pgm.mutation.types.KitMutation;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

public class ToolsMutation extends KitMutation{

    final static FreeItemKit[] TOOLS = new FreeItemKit[] {
            new FreeItemKit(new ItemBuilder(item(Material.DIAMOND_PICKAXE)).enchant(Enchantment.DIG_SPEED, 10).name("Quick Pick").unbreakable(true).get()),
            new FreeItemKit(new ItemBuilder(item(Material.DIAMOND_AXE)).enchant(Enchantment.DIG_SPEED, 10).name("Quick Axe").unbreakable(true).get()),
            new FreeItemKit(new ItemBuilder(item(Material.DIAMOND_SPADE)).enchant(Enchantment.DIG_SPEED, 10).name("Quick Shovel").unbreakable(true).get()),
            new FreeItemKit(new ItemBuilder(item(Material.SHEARS)).enchant(Enchantment.DIG_SPEED, 10).name("Quick Shears").unbreakable(true).get()),
            new FreeItemKit(new ItemBuilder(item(Material.GLASS)).amount(64).get())
    };

    final WeakHashMap<MatchPlayer, List<ItemStack>> toolsRemoved;

    public ToolsMutation(Match match) {
        super(match, true, TOOLS);
        toolsRemoved = new WeakHashMap<>();
    }

    @Override
    public void apply(MatchPlayer player) {
        // Find the player's tools and store them for later
        List<ItemStack> hotbar = Slot.Hotbar.hotbar()
                .map(slot -> slot.getItem(player.getInventory()))
                .collect(Collectors.toList());
        List<ItemStack> toolsSaved = new ArrayList<ItemStack>();
        for(ItemStack item : hotbar) {
            if(item != null && ItemUtils.isTool(item)) {
                toolsSaved.add(item);
                player.getInventory().remove(item);
            }
        }
        if (!toolsSaved.isEmpty()) {
            toolsRemoved.put(player, toolsSaved);
        }
        super.apply(player);
    }

    @Override
    public void remove(MatchPlayer player) {
        super.remove(player);
        // Restore the player's old tools
        List<ItemStack> tools = toolsRemoved.remove(player);
        if (tools != null && !player.isObserving() && !player.getMatch().inState(MatchState.Finished)) {
            for (ItemStack tool : tools) {
                ItemKitApplicator applicator = new ItemKitApplicator();
                applicator.add(tool);
                applicator.apply(player);
            }
            tools.clear();
        }
    }

    @Override
    public void disable() {
        super.disable();
        toolsRemoved.clear();
    }
}