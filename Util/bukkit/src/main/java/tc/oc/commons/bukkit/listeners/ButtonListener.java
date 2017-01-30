package tc.oc.commons.bukkit.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public interface ButtonListener {

    /**
     * Called when a button is clicked in some way, either through an inventory window,
     * or while being held by the player. In the latter case, the {@link ClickType} will
     * be either {@link ClickType#LEFT} or {@link ClickType#RIGHT}
     *
     * @return true to cancel the default behavior of the click
     */
    boolean buttonClicked(ItemStack button, Player clicker, ClickType clickType, Event event);
}
