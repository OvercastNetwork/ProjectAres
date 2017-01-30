package tc.oc.pgm.cycle;

import java.util.List;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import org.bukkit.command.CommandSender;
import java.time.Duration;
import javax.inject.Singleton;

import tc.oc.commons.core.commands.Commands;
import tc.oc.pgm.PGMTranslations;
import tc.oc.pgm.commands.CommandUtils;
import tc.oc.pgm.map.PGMMap;
import tc.oc.pgm.match.Match;

@Singleton
public class CycleCommands implements Commands {
    private static final String PERMISSION = "pgm.cycle";

    private void cycle(CommandSender sender, Duration countdown, PGMMap map, boolean force) throws CommandException {
        Match match = CommandUtils.getMatch(sender);
        CycleMatchModule cmm = match.needMatchModule(CycleMatchModule.class);

        if(match.isRunning() && !force && !cmm.getConfig().runningMatch()) {
            throw new CommandException(PGMTranslations.get().t("command.admin.cycle.matchRunning", sender));
        }

        if(map != null) {
            cmm.startCountdown(countdown, map);
        } else {
            cmm.startCountdown(countdown);
        }
    }

    @Command(
        aliases = {"cycle"},
        desc = "Queues a cycle to the next map in a certain amount of seconds",
        usage = "[seconds] [mapname]",
        flags = "f"
    )
    @CommandPermissions(PERMISSION)
    public List<String> cycle(CommandContext args, CommandSender sender) throws CommandException {
        if(args.getSuggestionContext() != null) {
            if(args.getSuggestionContext().getIndex() >= 1) {
                return CommandUtils.completeMapName(args.getJoinedStrings(1));
            }
            return null;
        }

        // Try to parse "<seconds> [mapname]", fall back to "<mapname>"
        // So the map can be given without the time

        Duration countdown;
        try {
            countdown = tc.oc.commons.bukkit.commands.CommandUtils.getDuration(args, 0);
        } catch(CommandException e) {
            countdown = null;
        }

        int index = countdown == null ? 0 : 1;
        String mapName = index < args.argsLength() ? args.getJoinedStrings(index) : null;

        cycle(sender, countdown, mapName == null ? null : CommandUtils.getMap(mapName, sender), args.hasFlag('f'));
        return null;
    }

    @Command(
        aliases = {"recycle", "rematch"},
        desc = "Reload (cycle to) the current map",
        usage = "[seconds]",
        flags = "f"
    )
    @CommandPermissions(PERMISSION)
    public void recycle(CommandContext args, CommandSender sender) throws CommandException {
        cycle(sender, tc.oc.commons.bukkit.commands.CommandUtils.getDuration(args, 0, Duration.ZERO), CommandUtils.getMatch(sender).getMap(), args.hasFlag('f'));
    }

}
