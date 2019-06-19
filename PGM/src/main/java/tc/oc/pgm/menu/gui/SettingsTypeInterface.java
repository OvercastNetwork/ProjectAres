package tc.oc.pgm.menu.gui;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import tc.oc.commons.bukkit.gui.buttons.Button;
import tc.oc.commons.bukkit.gui.buttons.empty.EmptyButton;
import tc.oc.commons.bukkit.gui.interfaces.SinglePageInterface;
import tc.oc.commons.bukkit.util.Constants;
import tc.oc.commons.bukkit.util.ItemCreator;

import tc.oc.pgm.PGMTranslations;

import java.util.ArrayList;
import java.util.List;



public class SettingsTypeInterface extends SinglePageInterface {

    public SettingsTypeInterface(Player player) {
        super(player, new ArrayList<>(), 27, "Setting Types");
        update();
    }

    @Override
    public void setButtons() {
        List<Button> buttons = new ArrayList<>();
        buttons.add(new Button(
                new ItemCreator(Material.BOOK_AND_QUILL)
                        .setName(Constants.PREFIX + PGMTranslations.get().t("setting.types.chat", player))
                , 11) {
            @Override
            public void function(Player player) {
                player.openInventory(new SettingsInterface(player, SettingMenuHelper.SettingType.CHAT).getInventory());
            }
        });
        buttons.add(new Button(
                new ItemCreator(Material.DIAMOND_SWORD)
                        .setName(Constants.PREFIX + PGMTranslations.get().t("setting.types.gameplay", player))
                , 13) {
            @Override
            public void function(Player player) {
                player.openInventory(new SettingsInterface(player, SettingMenuHelper.SettingType.GAMEPLAY).getInventory());
            }
        });
        buttons.add(new Button(
                new ItemCreator(Material.SLIME_BALL)
                        .setName(Constants.PREFIX + PGMTranslations.get().t("setting.types.misc", player))
                , 15) {
            @Override
            public void function(Player player) {
                player.openInventory(new SettingsInterface(player, SettingMenuHelper.SettingType.MISC).getInventory());
            }
        });

        setButtons(buttons);
    }


    @Override
    public void setDefaultButtons() {
        defaultButtons.clear();

        // "Go back" button
        defaultButtons.add(new Button(new ItemCreator(Material.WOOL)
                .setData(14)
                .setName(ChatColor.GREEN + "Go Back"), 22) {
            @Override
            public void function(Player player) {
                player.openInventory(new MainMenuInterface(player).getInventory());
            }
        });

        // Create empty buttons at unused slots
        for (Integer integer : new Integer[]{
                0,  1,  2,  3,  4,  5,  6,  7,  8,
                9, 10,     12,     14,     16,  17,
                18,19, 20, 21,     23, 24, 25,  26}) {
            EmptyButton button = new EmptyButton(integer);
            defaultButtons.add(button);
        }
    }

}
