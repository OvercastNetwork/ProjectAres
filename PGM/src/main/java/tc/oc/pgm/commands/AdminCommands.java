package tc.oc.pgm.commands;

import java.util.List;
import javax.inject.Inject;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Console;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import java.time.Duration;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.commands.TranslatableCommandException;
import tc.oc.commons.core.restart.RestartManager;
import tc.oc.commons.core.util.TimeUtils;
import tc.oc.pgm.PGM;
import tc.oc.pgm.PGMTranslations;
import tc.oc.pgm.blitz.BlitzMatchModule;
import tc.oc.pgm.blitz.BlitzMatchModuleImpl;
import tc.oc.pgm.blitz.BlitzProperties;
import tc.oc.pgm.blitz.Lives;
import tc.oc.pgm.map.PGMMap;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchManager;
import tc.oc.pgm.match.MatchState;
import tc.oc.pgm.restart.RestartListener;
import tc.oc.pgm.victory.VictoryMatchModule;
import tc.oc.pgm.rotation.RotationManager;
import tc.oc.pgm.rotation.RotationState;

public class AdminCommands implements Commands {
    
    private final RestartManager restartManager;
    private final RestartListener restartListener;

    @Inject AdminCommands(RestartManager restartManager, RestartListener restartListener) {
        this.restartManager = restartManager;
        this.restartListener = restartListener;
    }

    @Command(
        aliases = {"restart"},
        desc = "Queues a server restart after a certain amount of time",
        usage = "[seconds] - defaults to 30 seconds",
        flags = "f",
        min = 0,
        max = 1
    )
    @CommandPermissions("server.restart")
    public void restart(CommandContext args, CommandSender sender) throws CommandException {
        // Countdown defers automatic restart, so don't allow excessively long times
        Duration countdown = TimeUtils.min(
            tc.oc.commons.bukkit.commands.CommandUtils.getDuration(args, 0, Duration.ofSeconds(30)),
            Duration.ofMinutes(5)
        );

        final Match match = CommandUtils.getMatch(sender);
        if(!match.canAbort() && !args.hasFlag('f')) {
            throw new CommandException(PGMTranslations.get().t("command.admin.restart.matchRunning", sender));
        }

        restartListener.queueRestart(match, countdown, "/restart command");
    }

    @Command(
            aliases = {"postponerestart", "pr"},
            usage = "[matches]",
            desc = "Cancels any queued restarts and postpones automatic restart to at least " +
                   "the given number of matches from now (default and maximum is 10).",
            min = 0,
            max = 1
    )
    @CommandPermissions("server.cancelrestart")
    @Console
    public void postponeRestart(CommandContext args, CommandSender sender) throws CommandException {
        final Integer matches = restartListener.restartAfterMatches(CommandUtils.getMatch(sender), args.getInteger(0, 10));
        if(matches == null) {
            restartManager.cancelRestart();
            sender.sendMessage(ChatColor.RED + "Automatic match count restart disabled");
        } else if(matches > 0) {
            restartManager.cancelRestart();
            sender.sendMessage(ChatColor.RED + "Server will restart automatically in " + matches + " matches");
        } else if(matches == 0) {
            sender.sendMessage(ChatColor.RED + "Server will restart automatically after the current match");
        }
    }

    @Command(
        aliases = {"end", "finish"},
        desc = "Ends the current running match, optionally with a winner",
        usage = "[competitor]",
        min = 0,
        max = -1
    )
    @CommandPermissions("pgm.end")
    public void end(CommandContext args, CommandSender sender) throws CommandException {
        Match match = PGM.getMatchManager().getCurrentMatch(sender);

        Competitor winner = null;
        if(args.argsLength() > 0) {
            winner = CommandUtils.getCompetitor(args.getJoinedStrings(0), sender);
        }

        if(match.isFinished()) {
            throw new TranslatableCommandException("command.admin.start.matchFinished");
        } else if(!match.canTransitionTo(MatchState.Finished)) {
            throw new TranslatableCommandException("command.admin.end.unknownError");
        }

        if(winner != null) {
            match.needMatchModule(VictoryMatchModule.class).setImmediateWinner(winner);
        }

        match.end();
    }

    @Command(
        aliases = {"setnext", "sn"},
        desc = "Sets the next map.  Note that the rotation will go to this map then resume as normal.",
        usage = "[map name]",
        flags = "f",
        min = 1,
        max = -1
    )
    @CommandPermissions("pgm.next.set")
    public List<String> setnext(CommandContext args, CommandSender sender) throws CommandException {
        final String mapName = args.argsLength() > 0 ? args.getJoinedStrings(0) : "";
        if(args.getSuggestionContext() != null) {
            return CommandUtils.completeMapName(mapName);
        }

        MatchManager mm = PGM.getMatchManager();
        boolean restartQueued = restartManager.isRestartRequested(ServerDoc.Restart.Priority.NORMAL);

        if (restartQueued && !args.hasFlag('f')) {
            throw new CommandException(PGMTranslations.get().t("command.admin.setNext.restartQueued", sender));
        }

        mm.setNextMap(CommandUtils.getMap(mapName, sender));
        if (restartQueued) {
            restartManager.cancelRestart();
            sender.sendMessage(ChatColor.GREEN + PGMTranslations.get().t("command.admin.cancelRestart.restartUnqueued", sender));
        }
        sender.sendMessage(ChatColor.DARK_PURPLE + PGMTranslations.get().t("command.admin.set.success", sender, ChatColor.GOLD + mm.getNextMap().getInfo().name + ChatColor.DARK_PURPLE));
        return null;
    }

    @Command(
        aliases = {"cancel"},
        desc = "Cancels all active PGM countdowns and disables auto-start and auto-cycle for the current match",
        min = 0,
        max = 0
    )
    @CommandPermissions("pgm.cancel")
    public void cancel(CommandContext args, CommandSender sender) throws CommandException {
        CommandUtils.getMatch(sender).countdowns().cancelAll(true);
        sender.sendMessage(ChatColor.GREEN + PGMTranslations.get().t("command.admin.cancel.success", sender));
    }

    @Command(
        aliases = {"skip"},
        desc = "Skip maps in the rotation",
        usage = "[n] - defaults to 1",
        min = 0,
        max = 1
    )
    @CommandPermissions("pgm.skip")
    public void skip(CommandContext args, CommandSender sender) throws CommandException {
        RotationManager manager = PGM.getMatchManager().getRotationManager();
        RotationState rotation = manager.getRotation();

        if(args.argsLength() > 0) {
            int n = args.getInteger(0, 1);
            rotation = rotation.skip(n);
            sender.sendMessage(
                    ChatColor.GREEN +
                    PGMTranslations.get().t("command.admin.skip.successMultiple", sender,
                            PGMTranslations.get().t(n == 1 ? "maps.singularCompound" : "maps.pluralCompound", sender, n),
                            rotation.getNext().getInfo().getShortDescription(sender) + ChatColor.GREEN
                    )
            );
        } else {
            PGMMap skippedMap = rotation.getNext();
            rotation = rotation.skip(1);
            sender.sendMessage(ChatColor.GREEN + PGMTranslations.get().t("command.admin.skip.success", sender, skippedMap.getInfo().getShortDescription(sender) + ChatColor.GREEN));
        }

        manager.setRotation(rotation);
    }

    @Command(
        aliases = {"skipto"},
        desc = "Skip to a certain point in the rotation",
        usage = "[n]",
        min = 1,
        max = 1
    )
    @CommandPermissions("pgm.skip")
    public void skipto(CommandContext args, CommandSender sender) throws CommandException {
        RotationManager manager = PGM.getMatchManager().getRotationManager();
        RotationState rotation = manager.getRotation();

        int newNextId = args.getInteger(0) - 1;
        if(RotationState.isNextIdValid(rotation.getMaps(), newNextId)) {
            rotation = rotation.skipTo(newNextId);
            manager.setRotation(rotation);
            sender.sendMessage(ChatColor.GREEN + PGMTranslations.get().t("command.admin.skipto.success", sender, rotation.getNext().getInfo().getShortDescription(sender) + ChatColor.GREEN));
        } else {
            throw new CommandException(PGMTranslations.get().t("command.admin.skipto.invalidPoint", sender));
        }
    }

    @Command(
        aliases = {"pgm"},
        desc = "Reload the PGM configuration",
        usage = "",
        min = 0,
        max = 0
    )
    @CommandPermissions("pgm.reload")
    public void pgm(CommandContext args, CommandSender sender) throws CommandException {
        PGM.get().reloadConfig();

        sender.sendMessage(ChatColor.GREEN + PGMTranslations.get().t("command.admin.pgm", sender));
    }

    @Command(
            aliases = {"blitz"},
            desc = "Enable blitz on the fly",
            usage = "<lives> <type>",
            min = 0,
            max = 2
    )
    @CommandPermissions("pgm.blitz")
    public void blitz(CommandContext args, CommandSender sender) throws CommandException {
        Match match = CommandUtils.getMatch(sender);
        BlitzMatchModule blitz = match.needMatchModule(BlitzMatchModuleImpl.class);
        if(blitz.activated()) {
            blitz.deactivate();
        }
        int lives = args.getInteger(0, 1);
        Lives.Type type = tc.oc.commons.bukkit.commands.CommandUtils.getEnum(args, sender, 1, Lives.Type.class, Lives.Type.INDIVIDUAL);
        if(lives >= 1) {
            blitz.activate(BlitzProperties.create(match, lives, type));
        }
    }

}
