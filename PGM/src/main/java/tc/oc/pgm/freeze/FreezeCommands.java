package tc.oc.pgm.freeze;

import javax.inject.Inject;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.CommandSender;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.commands.UserFinder;
import tc.oc.minecraft.scheduler.MainThreadExecutor;
import tc.oc.commons.core.commands.CommandFutureCallback;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.commands.ComponentCommandException;

public class FreezeCommands implements Commands {

    private final Freeze freeze;
    private final UserFinder userFinder;
    private final MainThreadExecutor executor;
    private final Audiences audiences;

    @Inject FreezeCommands(Freeze freeze, UserFinder userFinder, MainThreadExecutor executor, Audiences audiences) {
        this.freeze = freeze;
        this.userFinder = userFinder;
        this.executor = executor;
        this.audiences = audiences;
    }

    @Command(
        aliases = { "freeze", "f" },
        usage = "<player>",
        desc = "Freeze a player",
        min = 1,
        max = 1
    )
    @CommandPermissions(Freeze.PERMISSION)
    public void freeze(final CommandContext args, final CommandSender sender) throws CommandException {
        if(!freeze.enabled()) {
            throw new ComponentCommandException(new TranslatableComponent("command.freeze.notEnabled"));
        }

        executor.callback(
            userFinder.findLocalPlayer(sender, args, 0),
            CommandFutureCallback.onSuccess(sender, args, response -> freeze.toggleFrozen(sender, response.player()))
        );
    }
}
