package tc.oc.pgm.tokens.gui;

import com.sk89q.minecraft.util.commands.CommandException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.commons.bukkit.gui.buttons.Button;
import tc.oc.commons.bukkit.gui.interfaces.ChestInterface;
import tc.oc.commons.bukkit.util.ItemCreator;
import tc.oc.pgm.PGM;
import tc.oc.pgm.commands.PollCommands;
import tc.oc.pgm.mutation.Mutation;
import tc.oc.pgm.mutation.MutationMatchModule;
import tc.oc.pgm.polls.PollMutation;

import java.util.ArrayList;
import java.util.List;

public class MutationConfirmInterface extends ChestInterface {
    private Mutation mutation;

    public MutationConfirmInterface(Player player, Mutation mutation) {
        super(player, new ArrayList<Button>(), 27, "Confirmation Menu");
        updateButtons();
        this.mutation = mutation;
    }

    @Override
    public void updateButtons() {
        List<Button> buttons = new ArrayList<>();

        buttons.add(new Button(new ItemCreator(Material.WOOL)
                .setData(5)
                .setName( ChatColor.GREEN + "Confirm" ), 12){
            @Override
            public void function(Player player) {
                MutationMatchModule module = PGM.getMatchManager().getCurrentMatch(player).getMatchModule(MutationMatchModule.class);
                try {
                    PollCommands.PollSubCommands.startPoll(new PollMutation(PGM.getPollManager(), Bukkit.getServer(), (CommandSender)player, mutation, module));
                    player.closeInventory();
                } catch (CommandException e) {
                    player.sendMessage(ChatColor.RED + "Another poll is already running.");
                    player.closeInventory();
                }
            }
        });

        buttons.add(new Button(new ItemCreator(Material.WOOL)
                .setData(14)
                .setName( ChatColor.GREEN + "Cancel" ), 14){
            @Override
            public void function(Player player) {
                player.openInventory(new MutationTokenInterface(player).getInventory());
            }
        });

        setButtons(buttons);
        updateInventory();
    }

}
