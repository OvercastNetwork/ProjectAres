package tc.oc.pgm.timelimit;

import java.time.Duration;
import javax.inject.Inject;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.CommandSender;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.chat.ComponentRenderers;
import tc.oc.commons.bukkit.commands.CommandUtils;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.commands.TranslatableCommandException;
import tc.oc.commons.core.formatting.PeriodFormats;
import tc.oc.commons.core.util.Comparables;
import tc.oc.pgm.PGM;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.victory.DefaultResult;
import tc.oc.pgm.victory.MatchResult;
import tc.oc.pgm.victory.VictoryResultParser;

public class TimeLimitCommands implements Commands {

    private final Audiences audiences;

    @Inject TimeLimitCommands(Audiences audiences) {
        this.audiences = audiences;
    }

    private void sendTimeLimitMessage(CommandSender sender, Duration duration, MatchResult result) {
        ComponentRenderers.send(sender, new Component(ChatColor.YELLOW)
            .extra(new TranslatableComponent("timeLimit.commandOutput",
                                             new Component(PeriodFormats.formatColonsPrecise(duration), ChatColor.AQUA),
                                             result.describeResult())));
    }

    @Command(
        aliases = {"timelimit", "tl"},
        desc = "Start, update, or cancel a time limit",
        usage = "cancel | [-r result] [duration]",
        help = "Result can be 'default', 'objectives', 'tie', or the name of a team",
        flags = "r:",
        min = 0,
        max = 1
    )
    public void timelimit(CommandContext commandContext, CommandSender sender) throws CommandException {
        Match match = PGM.getMatchManager().getCurrentMatch(sender);
        TimeLimitMatchModule tlmm = match.getMatchModule(TimeLimitMatchModule.class);
        TimeLimit existing = tlmm.getTimeLimit();
        final Audience audience = audiences.get(sender);

        String resultString = commandContext.getFlag('r', null);
        String durationString = commandContext.getString(0, null);

        if(resultString == null && durationString == null) {
            if(existing != null) {
                if(match.isFinished() && tlmm.remaining() != null) {
                    audience.sendMessage(new TranslatableComponent(
                        "timeLimit.matchEndRemaining",
                        new Component(PeriodFormats.formatColonsPrecise(tlmm.remaining()), ChatColor.AQUA))
                    );
                } else {
                    sendTimeLimitMessage(sender, existing.getDuration(), existing.result());
                }
            } else {
                audience.sendMessage(new TranslatableComponent("timeLimit.none"));
            }
        } else {
            if(!sender.hasPermission("pgm.timelimit")) {
                throw new CommandPermissionsException();
            }

            if("cancel".equals(durationString)) {
                if(tlmm.getTimeLimit() != null) {
                    tlmm.setTimeLimit(null);
                    audience.sendMessage(new TranslatableComponent("timeLimit.cancelled"));
                } else {
                    audience.sendMessage(new TranslatableComponent("timeLimit.none"));
                }
            } else {
                MatchResult result;
                if(resultString != null) {
                    result = VictoryResultParser.parse(match, resultString);
                } else if(existing != null) {
                    result = existing.result();
                } else {
                    result = new DefaultResult();
                }

                Duration duration;
                if(durationString != null) {
                    duration = CommandUtils.getDuration(durationString);
                    if(Comparables.greaterThan(duration, TimeLimit.MAX_DURATION)) {
                        throw new TranslatableCommandException("timeLimit.maxDays", TimeLimit.MAX_DURATION.toDays());
                    }
                } else if(existing != null) {
                    duration = existing.getDuration();
                } else {
                    throw new CommandException("Please specify a duration");
                }

                tlmm.setTimeLimit(new TimeLimit(match, duration, result, true));
                tlmm.start();

                sendTimeLimitMessage(sender, duration, result);
            }
        }
    }
}
