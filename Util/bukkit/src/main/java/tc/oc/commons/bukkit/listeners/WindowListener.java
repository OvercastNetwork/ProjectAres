package tc.oc.commons.bukkit.listeners;

import javax.annotation.Nullable;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public interface WindowListener {

    /**
     * Called when a window is opened, or when the listener is registered to an already open window.
     */
    default void windowOpened(InventoryView window) {}

    /**
     * Called when a window is closed. This is guaranteed to be called exactly once after every call to
     * {@link #windowOpened(InventoryView)}, and before that method is called for any other window
     * opened by the same player.
     */
    default void windowClosed(InventoryView window) {}

    /**
     * Called when an open window is clicked.
     */
    default boolean windowClicked(InventoryView window,
                                  Inventory inventory,
                                  ClickType clickType,
                                  InventoryType.SlotType slotType,
                                  int slotIndex,
                                  @Nullable ItemStack item) {
        return false;
    }
}
