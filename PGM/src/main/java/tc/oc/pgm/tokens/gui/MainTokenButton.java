package tc.oc.pgm.tokens.gui;

import tc.oc.commons.bukkit.gui.buttons.Button;
import tc.oc.commons.bukkit.util.Constants;
import tc.oc.commons.bukkit.util.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class MainTokenButton extends Button {

    public MainTokenButton() {
        super(13);
    }

    @Override
    public ItemCreator getIcon() {
        return new ItemCreator(Material.DOUBLE_PLANT)
                .setName(Constants.PREFIX + "Tokens");
    }

    @Override
    public void function(Player player) {
        player.openInventory(new MainTokenMenu(player).getInventory());
    }
}
