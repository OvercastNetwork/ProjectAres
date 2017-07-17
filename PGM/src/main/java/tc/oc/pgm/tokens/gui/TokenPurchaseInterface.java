package tc.oc.pgm.tokens.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import tc.oc.commons.bukkit.gui.buttons.Button;
import tc.oc.commons.bukkit.gui.interfaces.ChestInterface;
import tc.oc.commons.bukkit.raindrops.RaindropUtil;
import tc.oc.commons.bukkit.tokens.TokenUtil;
import tc.oc.commons.bukkit.util.Constants;
import tc.oc.commons.bukkit.util.ItemCreator;

import java.util.ArrayList;
import java.util.List;

public class TokenPurchaseInterface extends ChestInterface {

    public TokenPurchaseInterface(Player player) {
        super(player, new ArrayList<Button>(), 36, "Purchase Tokens");
        updateButtons();
    }

    @Override
    public void updateButtons() {
        List<Button> buttons = new ArrayList<>();

        int numRaindrops = TokenUtil.getUser(player).raindrops();

        buttons.add(new Button(
                new ItemCreator(Material.GHAST_TEAR)
                        .setName(Constants.PREFIX + "Droplets")
                        .addLore(Constants.SUBTEXT + "You have "
                                + Integer.valueOf(numRaindrops) + " Droplet" + (numRaindrops == 1 ? "" : "s" ))
                , 13));

        buttons.add(new Button(
                new ItemCreator(Material.EMERALD)
                        .setName(Constants.PREFIX + "You can buy Tokens at")
                        .addLore(Constants.SUBTEXT + "stratusnetwork.buycraft.net")
                , 4));

        buttons.add(new Button(
                new ItemCreator(Material.WOOL)
                .setData(14)
                .setName( ChatColor.GREEN + "Go Back" ), 31){
            @Override
            public void function(Player player) {
                player.openInventory(new MainTokenMenu(player).getInventory());
            }
        });

        buttons.add(getMapPurchaseButton(1, 15000, 2));
        buttons.add(getMapPurchaseButton(3, 40000, 11));
        buttons.add(getMapPurchaseButton(5, 60000, 20));

        buttons.add(getMutationPurchaseButton(1, 10000, 6));
        buttons.add(getMutationPurchaseButton(3, 25000, 15));
        buttons.add(getMutationPurchaseButton(5, 40000, 24));

        setButtons(buttons);
        updateInventory();
    }

    private Button getMutationPurchaseButton(int amount, int cost, int slot) {
        return new Button(
                new ItemCreator(Material.ELYTRA)
                        .setName(Constants.PREFIX + "Buy " + amount + " Mutation Tokens")
                        .addLore(Constants.SUBTEXT + "Cost: " + cost + " Droplets")
                        .setSize(amount), slot){
            @Override
            public void function(Player player) {
                if (TokenUtil.getUser(player).raindrops() >= cost) {
                    RaindropUtil.giveRaindrops(TokenUtil.getUser(player), -cost, 100, null, null, true, true);
                    TokenUtil.giveMutationTokens(TokenUtil.getUser(player), amount);
                }
            }
        };
    }

    private Button getMapPurchaseButton(int amount, int cost, int slot) {
        return new Button(
                new ItemCreator(Material.ELYTRA)
                        .setName(Constants.PREFIX + "Buy " + amount + " SetNext Tokens")
                        .addLore(Constants.SUBTEXT + "Cost: " + cost + " Droplets")
                        .setSize(amount), slot){
            @Override
            public void function(Player player) {
                if (TokenUtil.getUser(player).raindrops() >= cost) {
                    RaindropUtil.giveRaindrops(TokenUtil.getUser(player), -cost, 100, null, null, true, true);
                    TokenUtil.giveMapTokens(TokenUtil.getUser(player), amount);
                }
            }
        };
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        player = null;
    }

}
