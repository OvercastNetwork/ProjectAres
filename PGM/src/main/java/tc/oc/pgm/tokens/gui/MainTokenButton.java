package tc.oc.pgm.tokens.gui;

import tc.oc.commons.bukkit.gui.buttons.Button;
import tc.oc.commons.bukkit.util.Constants;
import tc.oc.commons.bukkit.util.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class MainTokenButton extends Button {
    private static MainTokenButton instance;

    public MainTokenButton() {
        super(13);
        instance = this;
    }

    public static MainTokenButton getInstance() {
        return instance;
    }

    @Override
    public ItemCreator getIcon() {
        return new ItemCreator(Material.DOUBLE_PLANT)
                .setName(Constants.PREFIX + "Tokens")
                .addLore(Constants.SUBTEXT + "Open the Token Menu",
                        Constants.CLICK + "Right Click");
    }

    @Override
    public void function(Player player) {
        player.openInventory(new MainTokenMenu(player).getInventory());
    }
}
