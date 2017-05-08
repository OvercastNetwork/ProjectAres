package tc.oc.commons.bukkit.gui.buttons;

import org.bukkit.entity.Player;
import tc.oc.commons.bukkit.util.ItemCreator;

/**
 * Button's are used as a way to allow players to interact with inventories. Interfaces contain buttons. When a button
 * is clicked, the function() method is fired, which can customized in special buttons to perform functions which the
 * button defines.
 */

public class Button {

    private ItemCreator icon;
    private Integer slot = 0;

    public Button(ItemCreator icon) {
        setIcon(icon);
    }

    public Button(int slot) {
        setSlot(slot);
    }

    public Button(ItemCreator icon, int slot) {
        setIcon(icon);
        setSlot(slot);
    }

    public void setIcon(ItemCreator icon) {
        this.icon = icon;
    }

    /**
     * This is what the button looks like in an interface.
     * @return the item
     */
    public ItemCreator getIcon() {
        return this.icon;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    /**
     * If the page is looking for a specific slot for the item, this is where it will go.
     * @return the slot
     */
    public Integer getSlot() {
        return this.slot;
    }

    /**
     * This is an action that the button performs. Typically overrided with a new function to be used to carry out
     * various actions.
     */
    public void function(Player player) {

    }

}
