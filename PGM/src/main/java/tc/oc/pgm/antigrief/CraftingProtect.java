package tc.oc.pgm.antigrief;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;

public class CraftingProtect implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void cloneCraftingWindow(final PlayerInteractEvent event) {
        if(!AntiGrief.CraftProtect.enabled()) {
            return;
        }

        if(!event.isCancelled() && event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getPlayer().getOpenInventory().getType() == InventoryType.CRAFTING /* nothing open */) {
            Block block = event.getClickedBlock();
            if(block != null && block.getType() == Material.WORKBENCH && !event.getPlayer().isSneaking()) {
                // create the window ourself
                event.setCancelled(true);
                event.getPlayer().openWorkbench(null, true); // doesn't check reachable
            }
        }
    }
}
