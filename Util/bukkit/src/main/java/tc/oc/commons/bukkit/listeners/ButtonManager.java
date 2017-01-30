package tc.oc.commons.bukkit.listeners;

import java.util.UUID;
import javax.inject.Singleton;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import tc.oc.commons.bukkit.item.StringItemTag;
import tc.oc.commons.core.plugin.PluginFacet;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Generic service for binding individual {@link ItemStack}s to {@link ButtonListener}s,
 * to receive generalized click events.
 *
 * ItemStacks are connected to listeners through a string ID stored in the item metadata.
 * Any number of distinct stacks can be bound to a single listener.
 */
@Singleton
public class ButtonManager implements PluginFacet, Listener {

    private static final StringItemTag TAG = new StringItemTag("ButtonId", null);

    private final BiMap<String, ButtonListener> listeners = HashBiMap.create();

    private String createId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Register a {@link ButtonListener} under the given ID. If the listener
     * is already registered under the given ID, nothing is changed.
     * @return the given ID
     * @throws IllegalArgumentException if the given ID is already registered with a different listener,
     *                                  or the given listener is already registered with a different ID.
     */
    public String registerListener(String id, ButtonListener listener) {
        final ButtonListener old = listeners.get(id);
        checkArgument(old == null || old.equals(listener)); // Check for dupe ID (BiMap already checks for dupe listener)
        listeners.put(id, listener);
        return id;
    }

    /**
     * Register the given {@link ButtonListener} with a generated unique ID, and return the ID.
     * If the listener is already registered, it's current ID is returned.
     */
    public String registerListener(ButtonListener listener) {
        final String id = listeners.inverse().get(listener);
        return id != null ? id : registerListener(createId(), listener);
    }

    /**
     * Release all resources for the given listener ID
     */
    public void unregisterListener(String id) {
        listeners.remove(id);
    }

    /**
     * Release all resources for the given listener
     */
    public void unregisterListener(ButtonListener listener) {
        listeners.inverse().remove(listener);
    }
    /**
     * Create a button that is identical to the given {@link ItemStack},
     * that notifies the {@link ButtonListener} registered with the given ID.
     *
     * The returned stack may or may not be the one given (it usually isn't).
     *
     * @throws IllegalArgumentException if no listener is registered with the given ID
     */
    public ItemStack createButton(String listenerId, ItemStack button) {
        checkArgument(listeners.containsKey(listenerId));
        button = button.clone();
        TAG.set(button, listenerId);
        return button;
    }

    /**
     * Create a button that is identical to the given {@link ItemStack},
     * that notifies the given {@link ButtonListener}.
     *
     * The listener is registered if it is not already registered.
     *
     * The returned stack may or may not be the one given (it usually isn't).
     */
    public ItemStack createButton(ButtonListener listener, ItemStack button) {
        button = button.clone();
        TAG.set(button, registerListener(listener));
        return button;
    }

    private boolean onButtonClick(ItemStack button, Player clicker, ClickType click, Event event) {
        if(!TAG.has(button)) return false;
        final String id = TAG.get(button);

        final ButtonListener listener = listeners.get(id);
        if(listener == null) return false;

        return listener.buttonClicked(button, clicker, click, event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInteract(PlayerInteractEvent event) {
        if(!event.hasItem()) return;

        final ClickType click;
        switch(event.getAction()) {
            case LEFT_CLICK_AIR:
            case LEFT_CLICK_BLOCK:
                click = ClickType.LEFT;
                break;

            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
                click = ClickType.RIGHT;
                break;

            default:
                return;
        }

        if(onButtonClick(event.getItem(), event.getActor(), click, event)) {
            event.setUseItemInHand(Event.Result.DENY);
            event.setUseInteractedBlock(Event.Result.DENY);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if(event.getCurrentItem() != null && onButtonClick(event.getCurrentItem(), event.getActor(), event.getClick(), event)) {
            event.setCancelled(true);
        }
    }
}
