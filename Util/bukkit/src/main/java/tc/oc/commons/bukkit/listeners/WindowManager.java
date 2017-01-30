package tc.oc.commons.bukkit.listeners;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Singleton;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import tc.oc.commons.bukkit.inventory.InventoryUtils;
import tc.oc.commons.core.plugin.PluginFacet;

/**
 * Generic service for tracking inventory window state, and dispatching events to {@link WindowListener}s.
 */
@Singleton
public class WindowManager implements PluginFacet, Listener {

    private class View {
        final Player player;
        final InventoryView window;
        final WindowListener listener;

        View(Player player, InventoryView window, WindowListener listener) {
            this.player = player;
            this.window = window;
            this.listener = listener;
        }

        void notifyOpen() {
            listener.windowOpened(window);
        }

        void notifyClose() {
            listener.windowClosed(window);
        }
    }

    private final Map<Player, View> views = new HashMap<>();

    private void handleCloseWindow(Player player) {
        final View view = views.remove(player);
        if(view != null) {
            view.notifyClose();
        }
    }

    /**
     * Register the given {@link WindowListener} to receive notifications about the given {@link InventoryView}.
     */
    public InventoryView registerWindow(WindowListener listener, InventoryView window) {
        final Player player = (Player) window.getPlayer();

        final View old = views.get(player);
        if(old == null || !old.window.equals(window)) {
            if(old != null) {
                old.notifyClose();
            }

            final View view = new View(player, window, listener);
            views.put(player, view);
            view.notifyOpen();
        }

        return window;
    }

    /**
     * Open an {@link InventoryView} window for the given {@link Player} onto the given {@link Inventory},
     * and register the given {@link WindowListener} to the window.
     *
     * If the player currently has an inventory window open, it is closed and any listener it has is
     * properly notified.
     */
    public InventoryView openWindow(WindowListener listener, Player player, Inventory inventory) {
        closeWindow(player);
        return registerWindow(listener, player.openInventory(inventory));
    }

    /**
     * Close any inventory window the player has open, and notify any listener registered to it.
     */
    public void closeWindow(Player player) {
        player.closeInventory();
        handleCloseWindow(player);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        final View view = views.get(event.getActor());
        if(view != null) {
            if(view.window.equals(event.getView())) {
                if(view.listener.windowClicked(event.getView(),
                                               InventoryUtils.clickedInventory(event),
                                               event.getClick(),
                                               event.getSlotType(),
                                               event.getSlot(),
                                               event.getCurrentItem())) {
                    event.setCancelled(true);
                }
            } else {
                // If player clicked in a window other than the one we are tracking for them,
                // it must have already been closed somehow.
                handleCloseWindow(event.getActor());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClose(InventoryCloseEvent event) {
        handleCloseWindow((Player) event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        // Probably not necessary
        handleCloseWindow(event.getPlayer());
    }
}
