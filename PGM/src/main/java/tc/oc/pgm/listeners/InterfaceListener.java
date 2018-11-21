package tc.oc.pgm.listeners;

import org.bukkit.event.EventBus;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerQuitEvent;
import tc.oc.commons.bukkit.event.InterfaceOpenEvent;
import tc.oc.commons.bukkit.gui.Interface;
import tc.oc.commons.bukkit.gui.InterfaceManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import tc.oc.commons.bukkit.gui.buttons.Button;
import tc.oc.commons.bukkit.gui.buttons.empty.EmptyButton;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.pgm.events.ObserverInteractEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class InterfaceListener implements Listener, PluginFacet {
    private final EventBus eventBus;

    @Inject
    InterfaceListener(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @EventHandler
    public void onPlayerClick(InventoryClickEvent event) {
        Interface gui = InterfaceManager.getInterface(event.getView());
        Player player = ((Player) event.getWhoClicked());
        try {
            Interface playergui = InterfaceManager.getInterface(player.getInventory());
            if (playergui != null) {
                for (Button button : InterfaceManager.getButtons(playergui, event.getSlot())) {
                    if (button != null) {
                        event.setCancelled(true);
                        button.function(player);
                        return;
                    }
                }
            }
        } catch (Exception e) {

        }
        if (gui != null) {
            event.setCancelled(true);
            for (Button button : InterfaceManager.getButtons(gui, event.getRawSlot())) {
                if (button != null && !(button instanceof EmptyButton)) {
                    button.function(player);
                    player.updateInventory();
                }
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Interface gui = InterfaceManager.getInterface(event.getInventory().getHolder());
        if (gui != null) {
            eventBus.callEvent(new InterfaceOpenEvent(gui, (Player) event.getPlayer()));
        }
    }

    @EventHandler
    public void onObserverInteract(ObserverInteractEvent event) {
        if (event.getClickType() == ClickType.RIGHT) {
            Interface gui = InterfaceManager.getInterface(event.getPlayer().getInventory());
            if (gui != null) {
                Button button = InterfaceManager.getButton(gui, event.getPlayer().getBukkit().getItemInHand());
                if (button != null) {
                    event.setCancelled(true);
                    button.function(event.getPlayer().getBukkit());
                }
            }
        }
    }

    @EventHandler
    public void onPlayerleave(PlayerQuitEvent event) {
        InterfaceManager.cleanUp(event.getPlayer());
    }

}
