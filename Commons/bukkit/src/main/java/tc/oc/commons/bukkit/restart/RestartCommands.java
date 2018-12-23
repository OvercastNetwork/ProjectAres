package tc.oc.commons.bukkit.restart;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.Console;
import javax.inject.Inject;
import net.md_5.bungee.api.chat.TranslatableComponent;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.Audiences;
import tc.oc.commons.core.commands.CommandFutureCallback;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.commands.TranslatableCommandException;
import tc.oc.commons.core.restart.RestartManager;
import tc.oc.minecraft.api.command.CommandSender;
import tc.oc.minecraft.scheduler.SyncExecutor;

public class RestartCommands implements Commands {

    private final RestartManager restartManager;
    private final SyncExecutor syncExecutor;
    private final Audiences audiences;

    @Inject RestartCommands(RestartManager restartManager, SyncExecutor syncExecutor, Audiences audiences) {
        this.restartManager = restartManager;
        this.syncExecutor = syncExecutor;
        this.audiences = audiences;
    }

    @Command(
        aliases = {"queuerestart", "qr"},
        desc = "Restart the server at the next safe opportunity",
        usage = "[-l/h (low/high priority)]",
        min = 0,
        max = 0,
        flags = "lh"
    )
    @CommandPermissions("server.queuerestart")
    @Console
    public void queueRestart(CommandContext args, final CommandSender sender) throws CommandException {
        final Audience audience = audiences.get(sender);

        final int priority;
        if(args.hasFlag('l')) {
            priority = ServerDoc.Restart.Priority.LOW;
        } else if(args.hasFlag('h')) {
            if(!sender.hasPermission("server.queuerestart.high")) {
                throw new CommandPermissionsException();
            }
            priority = ServerDoc.Restart.Priority.HIGH;
        } else {
            priority = ServerDoc.Restart.Priority.NORMAL;
        }

        if(restartManager.isRestartRequested(priority)) {
            audience.sendMessage(new TranslatableComponent("command.admin.queueRestart.restartQueued"));
            return;
        }

        syncExecutor.callback(
            restartManager.requestRestart("/queuerestart command", priority),
            CommandFutureCallback.onSuccess(sender, args, result -> {
                if(restartManager.isRestartDeferred()) {
                    audience.sendMessage(new TranslatableComponent("command.admin.queueRestart.restartQueued"));
                } else {
                    audience.sendMessage(new TranslatableComponent("command.admin.queueRestart.restartingNow"));
                }
            })
        );
    }

    @Command(
            aliases = {"cancelrestart", "cr"},
            desc = "Cancels a previously requested restart",
            min = 0,
            max = 0
    )
    @CommandPermissions("server.cancelrestart")
    @Console
    public void cancelRestart(CommandContext args, final CommandSender sender) throws CommandException {
        if(!restartManager.isRestartRequested()) {
            throw new TranslatableCommandException("command.admin.cancelRestart.noActionTaken");
        }

        syncExecutor.callback(
            restartManager.cancelRestart(),
            CommandFutureCallback.onSuccess(sender, args, o -> {
                audiences.get(sender).sendMessage(new TranslatableComponent("command.admin.cancelRestart.restartUnqueued"));
            })
        );
    }
}
