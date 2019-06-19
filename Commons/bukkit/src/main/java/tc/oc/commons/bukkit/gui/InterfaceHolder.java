package tc.oc.commons.bukkit.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public interface InterfaceHolder extends InventoryHolder {

    Inventory getInventory();

    Interface getInterface();

}
