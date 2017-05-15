package tc.oc.commons.bukkit.gui.interfaces;

import org.bukkit.inventory.Inventory;
import tc.oc.commons.bukkit.gui.Interface;

public class InterfaceInventory {

    private Interface gui;
    private Inventory inventory;

    public InterfaceInventory(Interface gui, Inventory inventory) {
        //InterfaceManager.registerInventory(this);
        this.gui = gui;
        this.inventory = inventory;
    }

    public Interface getInterface() {
        return this.gui;
    }

    public Inventory getInventory() {
        return this.inventory;
    }

}
