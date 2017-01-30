package tc.oc.commons.core.commands;

import java.util.Map;
import java.util.logging.Logger;
import javax.inject.Inject;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import tc.oc.api.docs.virtual.DeployInfo;
import tc.oc.api.minecraft.servers.StartupServerDocument;
import tc.oc.api.util.Permissions;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.scheduler.Scheduler;
import tc.oc.minecraft.api.command.CommandSender;
import tc.oc.minecraft.api.server.LocalServer;
import tc.oc.parse.primitive.DurationParser;

public class DebugCommands implements Commands {

    private final LocalServer minecraftServer;
    private final StartupServerDocument startupDocument;
    private final Scheduler scheduler;
    private final DurationParser durationParser;

    @Inject DebugCommands(LocalServer minecraftServer, StartupServerDocument startupDocument, Scheduler scheduler, DurationParser durationParser) {
        this.minecraftServer = minecraftServer;
        this.startupDocument = startupDocument;
        this.scheduler = scheduler;
        this.durationParser = durationParser;
    }

    @Command(
        aliases = "sleep",
        desc = "Put the main server thread to sleep for the given duration",
        usage = "<time>",
        flags = "",
        min = 1,
        max = 1
    )
    @CommandPermissions(Permissions.DEVELOPER)
    public void sleep(CommandContext args, CommandSender sender) throws CommandException {
        try {
            Thread.sleep(durationParser.parse(args.getString(0)).toMillis());
        } catch(InterruptedException e) {
            throw new CommandException("Sleep was interrupted", e);
        }
    }

    @Command(
        aliases = "throw",
        desc = "Throw a test RuntimeException",
        usage = "[message]",
        flags = "tr",
        min = 0,
        max = 1
    )
    @CommandPermissions(Permissions.DEVELOPER)
    public void throwError(CommandContext args, CommandSender sender) throws CommandException {
        if(args.hasFlag('r')) {
            Logger.getLogger("").severe("Test root logger error from " + sender.getName());
        } else if(args.hasFlag('t')) {
            scheduler.createTask(() -> {
                throwError(args, sender, "task");
            });
        } else {
            throwError(args, sender, "command");
        }
    }

    private void throwError(CommandContext args, CommandSender sender, String type) {
        final String message;
        if(args.argsLength() == 0) {
            message = "Test " + type + " error from " + sender.getName();
        } else {
            message = args.getJoinedStrings(0);
        }
        throw new RuntimeException(message);
    }

    @Command(
        aliases = "port",
        desc = "Get the server listening port",
        min = 0,
        max = 0
    )
    @CommandPermissions(Permissions.DEVELOPER)
    public void port(CommandContext args, CommandSender sender) throws CommandException {
        sender.sendMessage(String.valueOf(minecraftServer.getAddress()));
    }

    @Command(
        aliases = "deployinfo",
        desc = "What is deployed on this server?",
        min = 0,
        max = 0
    )
    @CommandPermissions(Permissions.DEVELOPER)
    public void deployInfo(CommandContext args, CommandSender sender) throws CommandException {
        final DeployInfo info = startupDocument.deploy_info();
        if(info == null) {
            throw new CommandException("No deploy info was loaded");
        } else {
            sender.sendMessage(new Component("Nextgen"));
            sender.sendMessage(new Component("  path: " + info.nextgen().path()));
            sender.sendMessage(new Component("  version: " + format(info.nextgen().version())));
            for(Map.Entry<String, DeployInfo.Version> entry : info.packages().entrySet()) {
                sender.sendMessage(new Component(entry.getKey() + ": " + format(entry.getValue())));
            }
        }
    }

    private static String format(DeployInfo.Version version) {
        return version.branch() + "/" + version.commit();
    }
}
