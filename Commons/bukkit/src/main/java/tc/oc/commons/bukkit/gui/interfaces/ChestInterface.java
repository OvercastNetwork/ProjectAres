package tc.oc.commons.bukkit.gui.interfaces;

import tc.oc.commons.bukkit.gui.Interface;
import tc.oc.commons.bukkit.gui.SimpleInterfaceHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import tc.oc.commons.bukkit.gui.buttons.Button;

import java.util.List;

public class ChestInterface extends Interface {

    private int size;
    private Inventory inventory;
    private String title;

    public ChestInterface(Player player, List<Button> buttons, int size, String title) {
        super(player, buttons);
        setSize(size);
        setTitle(title);
        this.inventory = Bukkit.createInventory(new SimpleInterfaceHolder(inventory, this, player.getWorld()), getSize(), getTitle());
        //setInventory(new InterfaceInventory(this, inventory));
    }

    public void setSize(int size) {
        //If the size isn't a multiple of 9, round it to the nearest multiple of 9.
        if (size % 9 != 0) {
            setSize(9*(Math.round(size / 9)));
        } else {
            this.size = size;
        }
    }

    public int getSize() {
        return this.size;
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
