package tc.oc.commons.bukkit.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import tc.oc.commons.bukkit.gui.buttons.Button;

import java.util.ArrayList;
import java.util.List;

public class InterfaceManager {

    private static List<Interface> inventories = new ArrayList<>();

    public static void registerInventory(Interface gui) {
        inventories.add(gui);
    }

    public static void unregisterInventories() {
        inventories.clear();
    }

    public static Interface getInterface(InventoryView inventory) {
        if (inventory.getTopInventory().getHolder() instanceof SimpleInterfaceHolder) {
            SimpleInterfaceHolder holder = (SimpleInterfaceHolder)inventory.getTopInventory().getHolder();
            return holder.getInterface();
        }
        return null;
    }

    public static Interface getInterface(InventoryHolder holder) {
        if (holder instanceof SimpleInterfaceHolder) {
            SimpleInterfaceHolder interfaceHolder = (SimpleInterfaceHolder)holder;
            return interfaceHolder.getInterface();
        }
        return null;
    }

    public static Interface getInterface(Inventory inventory) {
        for (Interface gui : inventories) {
            if (gui.getInventory().equals(inventory)) {
                return gui;
            }
        }
        return null;
    }

    public static List<Button> getButtons(Interface gui, int slot) {
        List<Button> buttons = new ArrayList<>();
        for (Button button : gui.getButtons()) {
            if (button.getSlot() == slot) {
                buttons.add(button);
            }
        }
        return buttons;
    }

    public static Button getButton(Interface gui, ItemStack itemStack) {
        for (Button button : gui.getButtons()) {
            if (button.getIcon().create().equals(itemStack)) {
                return button;
            }
        }
        return null;
    }

    public static void cleanUp(Interface inventory) {
        for (Interface inv: inventories) {
            if (inv != inventory && inv.getPlayer() == inventory.getPlayer()) {
                inv.cleanUp();
                inventories.remove(inv);
            }
        }
    }

    public static void cleanUp(Player player) {
        for (Interface inventory: inventories) {
            if (inventory.getPlayer() == player) {
                inventory.cleanUp();
                inventories.remove(inventory);
            }
        }
    }

}
