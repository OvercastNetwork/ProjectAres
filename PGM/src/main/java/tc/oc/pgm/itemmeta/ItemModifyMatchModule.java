package tc.oc.pgm.itemmeta;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.ItemStack;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchScope;

@ListenerScope(MatchScope.LOADED)
public class ItemModifyMatchModule extends MatchModule implements Listener {

    private final ItemModifyModule imm;

    public ItemModifyMatchModule(Match match) {
        super(match);
        this.imm = match.getModuleContext().needModule(ItemModifyModule.class);
    }

    private boolean applyRules(ItemStack stack) {
        return imm.applyRules(stack);
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        ItemStack stack = event.getEntity().getItemStack();
        if(applyRules(stack)) {
            event.getEntity().setItemStack(stack);
        }
    }

    @EventHandler
    public void onItemCraft(CraftItemEvent event) {
        ItemStack stack = event.getCurrentItem();
        if(applyRules(stack)) {
            event.setCurrentItem(stack);
            event.getInventory().setResult(stack);
        }
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        ItemStack stack = event.getInventory().getResult();
        if(applyRules(stack)) {
            event.getInventory().setResult(stack);
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        event.getInventory().contents().forEach(this::applyRules);
    }

    @EventHandler
    public void onArmorDispense(BlockDispenseEvent event) {
        // This covers armor being equipped by a dispenser, which does not call any of the other events
        ItemStack stack = event.getItem();
        if(applyRules(stack)) {
            event.setItem(stack);
        }
    }

    @EventHandler
    public void onArrowPickup(PlayerPickupArrowEvent event) {
        // Only needed for players picking up arrows stuck in blocks
        final ItemStack item = event.getItem().getItemStack();
        if(applyRules(item)) {
            event.getItem().setItemStack(item);
        }
    }
}
