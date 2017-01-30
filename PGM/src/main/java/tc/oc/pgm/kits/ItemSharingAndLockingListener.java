package tc.oc.pgm.kits;

import java.util.Iterator;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.inject.Inject;

import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import tc.oc.commons.bukkit.inventory.Slot;
import tc.oc.pgm.events.ItemTransferEvent;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.kits.tag.ItemTags;
import tc.oc.pgm.match.MatchPlayerFinder;
import tc.oc.pgm.match.MatchScope;

@ListenerScope(MatchScope.LOADED)
public class ItemSharingAndLockingListener implements Listener {

    private final MatchPlayerFinder playerFinder;

    @Inject private ItemSharingAndLockingListener(MatchPlayerFinder playerFinder) {
        this.playerFinder = playerFinder;
    }

    private boolean isLocked(@Nullable ItemStack item) {
        return item != null && ItemTags.LOCKED.get(item);
    }

    private boolean isUnshareable(@Nullable ItemStack item) {
        return item != null && (isLocked(item) || ItemTags.PREVENT_SHARING.get(item));
    }

    private void sendLockWarning(HumanEntity player) {
        playerFinder.player(player).ifPresent(
            mp -> mp.sendWarning(new TranslatableComponent("item.locked"), true)
        );
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInventoryClick(final InventoryClickEvent event) {
        if(event instanceof InventoryCreativeEvent) return;;

        // Ensure the player is clicking in their own inventory
        // TODO: should we allow items to be locked into other types of inventories?
        if(!Objects.equals(event.getWhoClicked(), event.getInventory().getHolder())) return;

        // Break out of the switch if the action will move a locked item, otherwise return
        switch(event.getAction()) {
            case HOTBAR_SWAP:
            case HOTBAR_MOVE_AND_READD:
                // These actions can move up to two stacks. Check the hotbar stack,
                // and then fall through to check the stack under the cursor.
                if(isLocked(Slot.Hotbar.forPosition(event.getHotbarButton())
                                       .getItem(event.getWhoClicked().getInventory()))) break;
                // fall through

            case PICKUP_ALL:
            case PICKUP_HALF:
            case PICKUP_SOME:
            case PICKUP_ONE:
            case SWAP_WITH_CURSOR:
            case MOVE_TO_OTHER_INVENTORY:
            case DROP_ONE_SLOT:
            case DROP_ALL_SLOT:
            case COLLECT_TO_CURSOR:
                // All these actions move only a single stack, except COLLECT_TO_CURSOR,
                // which can only move items that are stackable with the one under the cursor,
                // and locked items are only stackable with other locked items.
                if(isLocked(event.getCurrentItem())) break;

                // fall through

            default: return;
        }

        event.setCancelled(true);
        sendLockWarning(event.getWhoClicked());
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onDropItem(PlayerDropItemEvent event) {
        if(isLocked(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
            sendLockWarning(event.getPlayer());
        } else if(isUnshareable(event.getItemDrop().getItemStack())) {
            event.getItemDrop().remove();
        }
    }

    @EventHandler(priority =  EventPriority.LOW, ignoreCancelled = true)
    public void onTransferItem(ItemTransferEvent event) {
        if(event.getType() == ItemTransferEvent.Type.PLACE && isUnshareable(event.getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        for(Iterator<ItemStack> iterator = event.getDrops().iterator(); iterator.hasNext(); ) {
            if(isUnshareable(iterator.next())) iterator.remove();;
        }
    }
}
