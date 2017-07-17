package tc.oc.pgm.menu.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import tc.oc.commons.bukkit.gui.buttons.Button;
import tc.oc.commons.bukkit.util.Constants;
import tc.oc.commons.bukkit.util.ItemCreator;
import tc.oc.pgm.tokens.gui.MainTokenMenu;

public class MainMenuButton extends Button {

    public MainMenuButton() {
        super(new ItemCreator(Material.ENCHANTED_BOOK)
                .setName(Constants.PREFIX + "Main Menu")
                .addLore(Constants.SUBTEXT + "Open the Main Menu",
                         Constants.CLICK + "Right Click"));
    }

    @Override
    public void function(Player player) {
        player.openInventory(new MainMenuInterface(player).getInventory());
    }
}
