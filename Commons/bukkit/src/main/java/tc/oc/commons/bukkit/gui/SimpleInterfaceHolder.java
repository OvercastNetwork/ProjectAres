package tc.oc.commons.bukkit.gui;

import org.bukkit.World;
import org.bukkit.inventory.Inventory;

public class SimpleInterfaceHolder implements InterfaceHolder {

    private Inventory inventory;
    private Interface gui;
    private World world;

    public SimpleInterfaceHolder(Inventory inventory, Interface gui, World world) {
        this.inventory = inventory;
        this.gui = gui;
        this.world = world;
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    @Override
    public Interface getInterface() {
        return this.gui;
    }

    @Override
    public World getWorld() {
        return world;
    }
}
