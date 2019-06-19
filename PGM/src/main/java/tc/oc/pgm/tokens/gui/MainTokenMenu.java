package tc.oc.pgm.tokens.gui;

import org.bukkit.ChatColor;
import tc.oc.commons.bukkit.chat.WarningComponent;
import tc.oc.commons.bukkit.gui.buttons.Button;
import tc.oc.commons.bukkit.gui.interfaces.ChestInterface;
import tc.oc.commons.bukkit.tokens.TokenUtil;
import tc.oc.commons.bukkit.util.Constants;
import tc.oc.commons.bukkit.util.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import tc.oc.pgm.Config;
import tc.oc.pgm.menu.gui.MainMenuInterface;

import java.util.ArrayList;
import java.util.List;

public class MainTokenMenu extends ChestInterface {

    public MainTokenMenu(Player player) {
        super(player, new ArrayList<Button>(), 36, "Token Menu");
        updateButtons();
    }

    @Override
    public void updateButtons() {
        List<Button> buttons = new ArrayList<>();

        int numTokens = TokenUtil.getUser(player).maptokens();
        buttons.add(new Button(
                new ItemCreator(Material.MAP)
                        .setName(Constants.PREFIX + "SetNext Tokens")
                        .addLore(Constants.SUBTEXT + "You have "
                        + Integer.valueOf(numTokens) + " SetNext Token" + (numTokens == 1 ? "" : "s" ))
                , 11) {
            @Override
            public void function(Player player) {
                player.closeInventory();
                if (TokenUtil.getUser(player).maptokens() < 1) {
                    player.sendMessage(ChatColor.RED + "You do not have enough SetNext Tokens to set a map!");
                } else {
                    player.sendMessage(ChatColor.AQUA + "Use /poll next [mapname] to set a map.");
                }
            }
        });

        numTokens = TokenUtil.getUser(player).mutationtokens();
        buttons.add(new Button(
                new ItemCreator(Material.ELYTRA)
                        .setName(Constants.PREFIX + "Mutation Tokens")
                        .addLore(Constants.SUBTEXT + "You have "
                                + Integer.valueOf(numTokens) + " Mutation Token" + (numTokens == 1 ? "" : "s" ))
                , 15) {
            @Override
            public void function(Player player) {
                if (Config.Poll.enabled()) {
                    player.openInventory(new MutationTokenInterface(player).getInventory());
                } else {
                    player.sendMessage(new WarningComponent("poll.disabled"));
                }
            }
        });

        buttons.add(new Button(
                new ItemCreator(Material.EMERALD)
                        .setName(Constants.PREFIX + "Buy Tokens")
                , 13) {
            @Override
            public void function(Player player) {
                player.openInventory(new TokenPurchaseInterface(player).getInventory());
            }
        });

        buttons.add(new Button(new ItemCreator(Material.WOOL)
                .setData(14)
                .setName( ChatColor.GREEN + "Cancel" ), 31){
            @Override
            public void function(Player player) {
                player.openInventory(new MainMenuInterface(player).getInventory());
            }
        });

        setButtons(buttons);
        updateInventory();
    }
}
