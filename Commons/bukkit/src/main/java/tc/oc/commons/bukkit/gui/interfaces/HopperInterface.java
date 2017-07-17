package tc.oc.commons.bukkit.gui.interfaces;

import tc.oc.commons.bukkit.gui.Interface;
import tc.oc.commons.bukkit.gui.SimpleInterfaceHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import tc.oc.commons.bukkit.gui.buttons.Button;

import java.util.List;

public class HopperInterface extends Interface {

    private int size;
    private Inventory inventory;
    private String title;

    public HopperInterface(Player player, List<Button> buttons, String title, Interface parent) {
        super(player, buttons);
        setTitle(title);
        this.inventory = Bukkit.createInventory(new SimpleInterfaceHolder(inventory, this, player.getWorld()), InventoryType.HOPPER, getTitle());
        /*//this.inventory = player.getInventory();
        //inventory = Bukkit.createInventory(new SimpleInterfaceHolder(inventory, this), InventoryType.valueOf(args), getTitle());
        //setInventory(new InterfaceInventory(this, inventory));
        updateButtons();
        updateInventory();*/
    }

    public void setTitle(String title) {
        int titleSize = 32;
        this.title = title.length() > titleSize ? title.substring(0, titleSize - 1) : title;
    }

    public String getTitle() {
        return this.title;
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        inventory = null;
    }

}
