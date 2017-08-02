package tc.oc.pgm.ffa;

import com.sk89q.minecraft.util.commands.*;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import tc.oc.pgm.PGM;
import tc.oc.pgm.commands.CommandUtils;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.teams.Team;

public class FreeForAllCommands {
    private FreeForAllCommands() {}

    public static class Parent {
        @Command(
            aliases = {"players", "ffa"},
            desc = "Commands related to free-for-all matches",
            min = 1,
            max = -1
        )
        @NestedCommand({FreeForAllCommands.class})
        public static void players() {
        }
    }

    @Command(
        aliases = {"min"},
        desc = "Change the minimum number of players required to start the match.",
        min = 1,
        max = 1
    )
    @CommandPermissions("pgm.team.size")
    public static void min(CommandContext args, CommandSender sender) throws CommandException {
        FreeForAllMatchModule ffa = CommandUtils.getMatchModule(FreeForAllMatchModule.class, sender);
        if("default".equals(args.getString(0))) {
            ffa.setMinPlayers(null);
        } else {
            int minPlayers = args.getInteger(0);
            if(minPlayers < 0) throw new CommandException("min-players cannot be less than 0");
            if(minPlayers > ffa.getMaxPlayers()) throw new CommandException("min-players cannot be greater than max-players");
            ffa.setMinPlayers(minPlayers);
        }

        sender.sendMessage(ChatColor.WHITE + "Minimum players is now " + ChatColor.AQUA + ffa.getMinPlayers());
    }

    @Command(
        aliases = {"max", "size"},
        desc = "Change the maximum number of players allowed to participate in the match.",
        min = 1,
        max = 2
    )
    @CommandPermissions("pgm.team.size")
    public static void max(CommandContext args, CommandSender sender) throws CommandException {
        FreeForAllMatchModule ffa = CommandUtils.getMatchModule(FreeForAllMatchModule.class, sender);
        if("default".equals(args.getString(0))) {
            ffa.setMaxPlayers(null, null);
        } else {
            int maxPlayers = args.getInteger(0);
            if(maxPlayers < 0) throw new CommandException("max-players cannot be less than 0");

            int maxOverfill = args.argsLength() >= 2 ? args.getInteger(1) : maxPlayers;
            if(maxOverfill < maxPlayers) throw new CommandException("max-overfill cannot be less than max-players");

            if(maxPlayers < ffa.getMinPlayers()) throw new CommandException("max-players cannot be less than min-players");

            ffa.setMaxPlayers(maxPlayers, maxOverfill);
        }

        sender.sendMessage(ChatColor.WHITE + "Maximum players is now " + ChatColor.AQUA + ffa.getMaxPlayers() +
                           ChatColor.WHITE + " and overfill is " + ChatColor.AQUA + ffa.getMaxOverfill());
    }

    @Command(
            aliases = {"force"},
            desc = "Force a player to participate in the match.",
            usage = "<player>",
            min = 1,
            max = 2
    )
    @CommandPermissions("pgm.team.force")
    public static void force(CommandContext args, CommandSender sender) throws CommandException, SuggestException {
        MatchPlayer player = CommandUtils.findSingleMatchPlayer(args, sender, 0);
        FreeForAllMatchModule ffa = CommandUtils.getMatchModule(FreeForAllMatchModule.class, sender);
        ffa.forceJoin(player);
    }

}
