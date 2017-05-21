package tc.oc.commons.bukkit.gui.buttons.lastPage;

import tc.oc.commons.bukkit.gui.Interface;
import tc.oc.commons.bukkit.gui.InterfaceManager;
import tc.oc.commons.bukkit.gui.buttons.Button;
import tc.oc.commons.bukkit.gui.interfaces.ChestInterface;
import tc.oc.commons.bukkit.gui.interfaces.SinglePageInterface;
import tc.oc.commons.bukkit.util.Constants;
import tc.oc.commons.bukkit.util.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;


public class LastPageButton extends Button {

    private SinglePageInterface page;

    public LastPageButton(int slot) {
        super(null, slot);
    }

    public LastPageButton(SinglePageInterface gui, int slot) {
        super(null, slot);
        this.page = gui;
        this.setIcon(new ItemCreator(Material.BARRIER)
                .setName(Constants.PREFIX + "Previous"));
    }

    public SinglePageInterface getPage() {
        return this.page;
    }

    public Interface getLastPage(ChestInterface gui) {
        if (this.page == null) {
            return gui.getParent();
        }
        try {
            int page = this.page.page;
            Interface previousInterface = getPage().getParent() != null ? getPage().getParent() : gui;
            try {
                Interface gui1 = /*page != 0 ? new SinglePageInterface(this.page.rawButtons, this.page.getSize(), this.page.rawTitle, this.page.getParent(), this.page.page - 1) :*/ previousInterface;
                if (gui1 instanceof SinglePageInterface) {
                   // ((SinglePageInterface)gui1).updateButtons();
                }
                return gui1;
            } catch (Exception e) {
                return previousInterface;
            }
        } catch (Exception e) {
            return gui;
        }
    }

    @Override
    public void function(Player player) {
        Interface currentInterface = InterfaceManager.getInterface(player.getOpenInventory());
        //Interface lastPage = getLastPage((ChestInterface)currentInterface);
        //player.openInventory(lastPage.getInventory());
        if (currentInterface instanceof SinglePageInterface) {
            ((SinglePageInterface)currentInterface).openLastPage(player);
        } else {
            Interface parent = ((ChestInterface) currentInterface).getParent();
            player.openInventory(parent.getInventory());
            //parent.updateButtons();
        }
    }

}
