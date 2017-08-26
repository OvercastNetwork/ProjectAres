package tc.oc.commons.bukkit.gui.buttons.nextPage;

import tc.oc.commons.bukkit.gui.Interface;
import tc.oc.commons.bukkit.gui.InterfaceManager;
import tc.oc.commons.bukkit.gui.buttons.Button;
import tc.oc.commons.bukkit.gui.interfaces.ChestOptionsPageInterface;
import tc.oc.commons.bukkit.gui.interfaces.SinglePageInterface;
import tc.oc.commons.bukkit.util.Constants;
import tc.oc.commons.bukkit.util.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class NextPageButton extends Button {

    private SinglePageInterface page;

    public NextPageButton(int slot) {
        super(null, slot);
    }

    public NextPageButton(SinglePageInterface gui, int slot) {
        super(null, slot);
        this.page = gui;
    }

    public SinglePageInterface getNextPage(SinglePageInterface chestInterface) {
        try {
            if (chestInterface instanceof ChestOptionsPageInterface) {
                ChestOptionsPageInterface nextPage = new ChestOptionsPageInterface(this.page.rawButtons, this.page.getSize(), this.page.rawTitle, this.page, this.page.page + 1);
                nextPage.update();
                return nextPage != null ? nextPage : chestInterface;
            }
            SinglePageInterface nextPage = new SinglePageInterface(this.page.getPlayer(), this.page.rawButtons, this.page.getSize(), this.page.rawTitle, this.page.page + 1);
            nextPage.update();
            return nextPage != null ? nextPage : chestInterface;
        } catch (Exception e) {
            return chestInterface;
        }
    }

    @Override
    public ItemCreator getIcon() {
        return new ItemCreator(Material.ARROW)
                .setName(Constants.PREFIX + "Next");
    }

    @Override
    public void function(Player player) {
        Interface currentInterface = InterfaceManager.getInterface(player.getOpenInventory());
        //Interface nextPage = getNextPage((SinglePageInterface) currentInterface);
        ((SinglePageInterface)currentInterface).openNextPage();
    }

}
