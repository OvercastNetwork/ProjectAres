package tc.oc.pgm.menu.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import tc.oc.commons.bukkit.gui.buttons.Button;
import tc.oc.commons.bukkit.gui.interfaces.ChestInterface;
import tc.oc.commons.bukkit.tokens.TokenUtil;
import tc.oc.commons.bukkit.util.Constants;
import tc.oc.commons.bukkit.util.ItemCreator;
import tc.oc.pgm.tokens.gui.MainTokenButton;
import tc.oc.pgm.tokens.gui.MutationTokenInterface;
import tc.oc.pgm.tokens.gui.TokenPurchaseInterface;

import java.util.ArrayList;
import java.util.List;

public class MainMenuInterface extends ChestInterface {
    private static MainMenuInterface instance;

    public MainMenuInterface(Player player) {
        super(player, new ArrayList<>(), 27, "Main Menu", getInstance());
        updateButtons();
        instance = this;
    }

    @Override
    public ChestInterface getParent() {
        return getInstance();
    }

    public static MainMenuInterface getInstance() {
        return instance;
    }

    @Override
    public void updateButtons() {
        List<Button> buttons = new ArrayList<>();

        MainTokenButton.getInstance().setSlot(11);
        buttons.add(MainTokenButton.getInstance());

        buttons.add(new Button(
                new ItemCreator(Material.SKULL_ITEM)
                        .setName(Constants.PREFIX + "Stats")
                        .setData(3)
                , 13)); //TODO -- show stats in lore

        buttons.add(new Button(
                new ItemCreator(Material.BOOK_AND_QUILL)
                        .setName(Constants.PREFIX + "Settings")
                , 15) {
            @Override
            public void function(Player player) {
                player.openInventory(new SettingsInterface(player).getInventory());
            }
        });

        setButtons(buttons);
        updateInventory();
    }
}
