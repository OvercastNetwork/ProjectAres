package tc.oc.commons.bukkit.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.NestedCommand;
import org.bukkit.ChatColor;
import org.bukkit.Skin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.commands.NestedCommands;
import tc.oc.commons.core.plugin.PluginFacet;

public class SkinCommands implements NestedCommands, PluginFacet {
    public static class Parent implements Commands, PluginFacet {
        @Command(
            aliases = {"skin"},
            desc = "Commands to manipulate skins",
            min = 1,
            max = -1
        )
        @NestedCommand({SkinCommands.class})
        @CommandPermissions("skin.change")
        public void skin() {
        }
    }

    @Command(aliases = {"info"},
             desc = "Dump the encoded data for a player's skin",
             usage = "[player]")
    public void info(CommandContext args, CommandSender sender) throws CommandException {
        Skin skin = CommandUtils.getPlayerOrSelf(args, sender, 0).getSkin();
        sender.sendMessage(ChatColor.BLUE + "Textures: " + ChatColor.WHITE + skin.getData());
        sender.sendMessage(ChatColor.BLUE + "Signature: " + ChatColor.WHITE + skin.getSignature());
    }

    @Command(aliases = {"reset"},
             desc = "Reset a player's skin to their real one",
             usage = "[player]")
    public void reset(CommandContext args, CommandSender sender) throws CommandException {
        Player player = CommandUtils.getPlayerOrSelf(args, sender, 0);

        player.setSkin(null);
        sender.sendMessage(ChatColor.WHITE + "Reset the skin of " + player.getDisplayName(sender));
    }

    @Command(aliases = {"clone"},
             desc = "Clone one player's skin to another",
             usage = "<source> [target]",
             flags = "u")
    public void clone(CommandContext args, CommandSender sender) throws CommandException {
        Player source = CommandUtils.getPlayer(args, sender, 0);
        Player target = CommandUtils.getPlayerOrSelf(args, sender, 1);

        boolean unsigned = args.hasFlag('u');
        Skin skin = source.getSkin();
        if(unsigned) {
            skin = new Skin(skin.getData(), null);
        }

        target.setSkin(skin);
        sender.sendMessage(ChatColor.WHITE + "Cloned " + source.getDisplayName(sender) + ChatColor.WHITE + "'s skin to " + target.getDisplayName(sender));
    }

    @Command(aliases = {"none"},
             desc = "Clear a player's skin, making them steve/alex",
             usage = "[player]")
    public void none(CommandContext args, CommandSender sender) throws CommandException {
        Player player = CommandUtils.getPlayerOrSelf(args, sender, 0);

        player.setSkin(Skin.EMPTY);
        sender.sendMessage(ChatColor.WHITE + "Cleared the skin of " + player.getDisplayName(sender));
    }
}
