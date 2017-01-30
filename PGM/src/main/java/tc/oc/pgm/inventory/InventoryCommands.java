package tc.oc.pgm.inventory;

import javax.inject.Inject;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.commands.TranslatableCommandException;
import tc.oc.pgm.match.inject.MatchScoped;

import static tc.oc.commons.bukkit.commands.CommandUtils.findOnlinePlayer;
import static tc.oc.commons.bukkit.commands.CommandUtils.senderToPlayer;

@MatchScoped
public class InventoryCommands implements Commands {

    private final ViewInventoryMatchModule vimm;

    @Inject InventoryCommands(ViewInventoryMatchModule vimm) {
        this.vimm = vimm;
    }

    @Command(
        aliases = {"inventory", "inv", "vi"},
        desc = "View a player's inventory",
        usage = "<player>",
        min = 1,
        max = 1
    )
    public void inventory(CommandContext args, CommandSender sender) throws CommandException {
        final Player viewer = senderToPlayer(sender);
        Player holder = findOnlinePlayer(args, viewer, 0);

        if(vimm.canPreviewInventory(viewer, holder)) {
            vimm.previewInventory((Player) sender, holder.getInventory());
        } else {
            throw new TranslatableCommandException("player.inventoryPreview.notViewable");
        }
    }
}
