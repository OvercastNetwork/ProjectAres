package tc.oc.pgm.goals;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.commands.CommandUtils;
import tc.oc.pgm.teams.Team;
import tc.oc.pgm.teams.TeamMatchModule;

import java.util.ArrayList;
import java.util.List;

import static tc.oc.commons.core.util.Nullables.castOrNull;

public class GoalCommands {
    private GoalCommands() {}

    @Command(
        aliases = {"proximity", "prox"},
        desc = "Show stats about how close each competitor has been to each objective",
        min = 0,
        max = 0
    )
    @CommandPermissions("pgm.proximity")
    public static void proximity(CommandContext args, CommandSender sender) throws CommandException {
        Match match = CommandUtils.getMatch(sender);
        TeamMatchModule tmm = CommandUtils.getMatchModule(TeamMatchModule.class, sender);

        MatchPlayer matchPlayer = sender instanceof Player ? match.getPlayer((Player) sender) : null;
        if(matchPlayer != null && matchPlayer.isParticipating()) {
            throw new CommandException("The /proximity command is only available to observers");
        }

        List<String> lines = new ArrayList<>();
        for(Team team : tmm.getTeams()) {
            boolean teamHeader = false;
            final GoalMatchModule gmm = match.needMatchModule(GoalMatchModule.class);

            for(Goal<?> goal : gmm.getGoals(team)) {
                if(goal instanceof TouchableGoal && goal.isVisible()) {
                    TouchableGoal touchable = (TouchableGoal) goal;
                    ProximityGoal proximity = castOrNull(goal, ProximityGoal.class);

                    if(!teamHeader) {
                        lines.add(team.getColoredName());
                        teamHeader = true;
                    }

                    String line = ChatColor.WHITE + "  " + touchable.getColoredName() + ChatColor.WHITE;

                    if(touchable.isCompleted(team)) {
                        line += ChatColor.GREEN + " COMPLETE";
                    } else if(touchable.hasTouched(team)) {
                        line += ChatColor.YELLOW + " TOUCHED";
                    } else {
                        line += ChatColor.RED + " UNTOUCHED";
                    }

                    if(proximity != null && proximity.isProximityRelevant(team)) {
                        ProximityMetric metric = proximity.getProximityMetric(team);
                        if(metric != null) {
                            line += ChatColor.GRAY + " " + metric.description() + ": " +
                                    ChatColor.AQUA + String.format("%.2f", proximity.getMinimumDistance(team));
                        }
                    }

                    lines.add(line);
                }
            }
        }

        if(lines.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "There are no objectives that track proximity");
        } else {
            sender.sendMessage(lines.toArray(new String[lines.size()]));
        }
    }

}
