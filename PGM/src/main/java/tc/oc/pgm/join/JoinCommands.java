package tc.oc.pgm.join;

import javax.inject.Singleton;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import org.bukkit.command.CommandSender;
import tc.oc.commons.core.commands.Commands;
import tc.oc.pgm.PGMTranslations;
import tc.oc.pgm.commands.CommandUtils;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.teams.TeamMatchModule;

@Singleton
public class JoinCommands implements Commands {
    @Command(
        aliases = { "join", "jugar", "jouer", "spielen"},
        desc = "Joins the current match",
        usage = "[team] - defaults to random",
        flags = "f",
        min = 0,
        max = -1
    )
    @CommandPermissions(JoinMatchModule.JOIN_PERMISSION)
    public void join(CommandContext args, CommandSender sender) throws CommandException {
        MatchPlayer player = CommandUtils.senderToMatchPlayer(sender);
        Match match = player.getMatch();
        JoinMatchModule jmm = match.needMatchModule(JoinMatchModule.class);
        TeamMatchModule tmm = match.getMatchModule(TeamMatchModule.class);

        boolean force = sender.hasPermission("pgm.join.force") && args.hasFlag('f');
        Competitor chosenParty = null;

        if(args.argsLength() > 0) {
            if(args.getJoinedStrings(0).trim().toLowerCase().startsWith("obs")) {
                observe(args, sender);
                return;
            } else if(tmm != null) {
                // player wants to join a specific team
                chosenParty = tmm.bestFuzzyMatch(args.getJoinedStrings(0));
                if(chosenParty == null) throw new CommandException(PGMTranslations.get().t("command.teamNotFound", sender));
            }
        }

        jmm.requestJoin(player, force ? JoinMethod.FORCE : JoinMethod.USER, chosenParty);
    }

    public static final String OBSERVE_COMMAND = "observe";

    @Command(
        aliases = { OBSERVE_COMMAND, "obs", "spectate" },
        desc = "Observe the current match",
        min = 0,
        max = 0
    )
    @CommandPermissions(JoinMatchModule.JOIN_PERMISSION)
    public void observe(CommandContext args, CommandSender sender) throws CommandException {
        final MatchPlayer player = CommandUtils.senderToMatchPlayer(sender);
        player.getMatch().needMatchModule(JoinMatchModule.class).requestObserve(player);
    }
}
