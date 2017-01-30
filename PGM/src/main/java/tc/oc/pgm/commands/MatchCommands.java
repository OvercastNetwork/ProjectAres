package tc.oc.pgm.commands;

import javax.inject.Inject;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import org.bukkit.command.CommandSender;
import tc.oc.commons.core.commands.Commands;
import tc.oc.pgm.match.MatchFormatter;

public class MatchCommands implements Commands {

    private final MatchFormatter formatter;

    @Inject MatchCommands(MatchFormatter formatter) {
        this.formatter = formatter;
    }

    @Command(
        aliases = {"matchinfo", "match"},
        desc = "Shows information about the current match",
        min = 0,
        max = 0
    )
    @CommandPermissions("pgm.matchinfo")
    public void matchinfo(CommandContext args, CommandSender sender) throws CommandException {
        formatter.sendMatchInfo(sender, CommandUtils.getMatch(sender));
    }
}
