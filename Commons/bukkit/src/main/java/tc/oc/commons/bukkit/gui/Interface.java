package tc.oc.commons.bukkit.gui;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import tc.oc.commons.bukkit.gui.buttons.Button;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Interface {

    protected Player player;
    private List<Button> buttons = new ArrayList<>();
    private List<Object> data = new ArrayList<>();

    public Interface(Player viewer, List<Button> buttons, Object... data) {
        setData(data);
        setPlayer(viewer);
        setButtons(buttons);
        InterfaceManager.cleanUp(this);
    }

    public void setData(Object... data) {
        this.data.clear();
        Collections.addAll(this.data, data);
    }

    public List<Object> getData() {
        return this.data;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return this.player;
    }

    public void setButtons(List<Button> buttons) {
        this.buttons = buttons;
    }

    public List<Button> getButtons() {
        return this.buttons;
    }

    public void updateButtons() {
        updateInventory();
    }

    public void updateInventory() {
        try {
            long currentTime = System.currentTimeMillis();
            for (Button button : getButtons()) {
                getInventory().setItem(button.getSlot(), button.getIcon().create());
            }
            for (HumanEntity player : getInventory().getViewers()) {
                player.getOpenInventory().getTopInventory().clear();
                for (Button button : getButtons()) {
                    player.getOpenInventory().setItem(button.getSlot(), button.getIcon().create());
                }
//                player.openInventory(getInventory().getInventory());
            }
        } catch (Exception e) {

        }
    }

    public Inventory getInventory() {
        return null;
    }

    public void cleanUp() {
        player = null;
        buttons = null;
        data = null;
    }

}
