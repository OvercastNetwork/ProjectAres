package tc.oc.pgm.channels;

import com.github.rmsy.channels.Channel;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.PGMTranslations;
import tc.oc.pgm.commands.CommandUtils;

public class ChannelCommands {
    @Command(
            aliases = "t",
            desc = "Sends a message to the team channel (or sets the team channel to your default channel).",
            usage = "[message...]",
            min = 0,
            max = -1,
            anyFlags = true
    )
    public static void teamChat(CommandContext args, CommandSender sender) throws CommandException {
        MatchPlayer player = CommandUtils.senderToMatchPlayer(sender);

        if (player.getBukkit().hasPermission(ChannelMatchModule.TEAM_SEND_PERMISSION)) {
            ChannelMatchModule cmm = player.getMatch().needMatchModule(ChannelMatchModule.class);

            if (args.argsLength() == 0) {
                cmm.setTeamChat(player, true);
                player.sendMessage(new TranslatableComponent("command.chat.team.switchSuccess"));
            } else {
                Channel channel = cmm.getChannel(player.getParty());
                channel.sendMessage(args.getJoinedStrings(0), player.getBukkit());
                if (!player.getBukkit().hasPermission(channel.getListeningPermission())) {
                    sender.sendMessage(ChatColor.YELLOW + PGMTranslations.t("command.chat.team.success", player));
                }
            }
        } else {
            throw new CommandPermissionsException();
        }
    }
}
