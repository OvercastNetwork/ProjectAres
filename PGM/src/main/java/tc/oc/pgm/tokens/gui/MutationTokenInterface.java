package tc.oc.pgm.tokens.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import tc.oc.commons.bukkit.gui.buttons.Button;
import tc.oc.commons.bukkit.gui.buttons.empty.EmptyButton;
import tc.oc.commons.bukkit.gui.interfaces.SinglePageInterface;
import tc.oc.commons.bukkit.tokens.TokenUtil;
import tc.oc.commons.bukkit.util.Constants;
import tc.oc.commons.bukkit.util.ItemCreator;
import tc.oc.pgm.PGM;
import tc.oc.pgm.PGMTranslations;
import tc.oc.pgm.menu.gui.MainMenuInterface;
import tc.oc.pgm.mutation.Mutation;
import tc.oc.pgm.mutation.MutationMatchModule;
import tc.oc.pgm.mutation.command.MutationCommands;

import java.util.ArrayList;
import java.util.List;

public class MutationTokenInterface extends SinglePageInterface {

    public MutationTokenInterface(Player player) {
        super(player, new ArrayList<>(), 54, "Token Menu - Mutations");
        update();
    }

    @Override
    public void setButtons() {
        List<Button> buttons = new ArrayList<>();
        for (Mutation mutation : Mutation.values()) {
            if (mutation.isPollable()) {
                buttons.add(getMutationButton(mutation));
            }
        }
        setButtons(buttons);
    }

    private Button getMutationButton(Mutation mutation) {
        ItemCreator itemCreator = new ItemCreator(mutation.getGuiDisplay())
                .setName(Constants.PREFIX + getMutationName(mutation, this.getPlayer()))
                .addLore(Constants.SUBTEXT + getMutationDescription(mutation, this.getPlayer()))
                .setHideFlags(ItemCreator.HideFlag.ALL);
        if (MutationCommands.getInstance().getMutationQueue().mutations().contains(mutation)) {
            itemCreator.addEnchantment(Enchantment.DURABILITY, 1);
        }
        return new Button(itemCreator) {
            @Override
            public void function(Player player) {
                if (hasEnoughTokens(player)) {
                    player.closeInventory();
                    MutationMatchModule module = PGM.getMatchManager().getCurrentMatch(player).getMatchModule(MutationMatchModule.class);
                    if (MutationCommands.getInstance().getMutationQueue().mutations().contains(mutation)) {
                        player.sendMessage(ChatColor.RED + "The " + getMutationName(mutation, player)
                                + " mutation is already enabled!");
                    } else if (PGM.getMatchManager().getCurrentMatch(player).isStarting()) {
                        if (module.mutationsActive().contains(mutation)) {
                            player.sendMessage(ChatColor.RED + "The " + getMutationName(mutation, player)
                                    + " mutation is already enabled!");
                        }
                    } else {
                        player.openInventory(new MutationConfirmInterface(player, mutation).getInventory());
                    }
                } else {
                    player.closeInventory();
                    player.sendMessage(ChatColor.RED + "You do not have enough Mutation Tokens!");
                }
            }
        };
    }

    private String getMutationName(Mutation mutation, Player player) {
        return PGMTranslations.get().t(mutation.getName(), player);
    }

    private String getMutationDescription(Mutation mutation, Player player) {
        String description = PGMTranslations.get().t(mutation.getDescription(), player);
        return description.substring(0, 1).toUpperCase() + description.substring(1);
    }

    private boolean hasEnoughTokens(Player player) {
        return TokenUtil.getUser(player).mutationtokens() > 0;
    }

    @Override
    public void setDefaultButtons() {
        defaultButtons.clear();
        defaultButtons.add(new Button(new ItemCreator(Material.WOOL)
                .setData(14)
                .setName(ChatColor.GREEN + "Go Back"), 49) {
            @Override
            public void function(Player player) {
                player.openInventory(new MainTokenMenu(player).getInventory());
            }
        });
        for (Integer integer : new Integer[]{
                 0,  1,  2,  3,  4,  5,  6,  7,  8,
                 9,                             17,
                18,                             26,
                27,                             35,
                36,                             44,
                45, 46, 47, 48,     50, 51, 52, 53}) {
            EmptyButton button = new EmptyButton(integer);
            defaultButtons.add(button);
        }
    }

}
