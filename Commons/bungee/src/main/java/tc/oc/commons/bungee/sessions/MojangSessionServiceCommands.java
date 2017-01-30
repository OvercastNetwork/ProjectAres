package tc.oc.commons.bungee.sessions;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import tc.oc.commons.core.commands.Commands;

public class MojangSessionServiceCommands implements Commands {
    private final MojangSessionServiceMonitor monitor;

    @Inject MojangSessionServiceCommands(MojangSessionServiceMonitor monitor) {
        this.monitor = monitor;
    }

    @Command(
            aliases = {"session"},
            desc = "Session status control",
            usage = "[on|off|clear]",
            max = 1
    )
    @CommandPermissions("bungeecord.command.session")
    public void session(final CommandContext args, CommandSender sender) throws CommandException {
        if (args.argsLength() == 1) {
            String arg = args.getString(0);
            SessionState newState = parseSessionStateCommand(arg);
            if (newState == null) throw new CommandException("Unknown session state command: " + arg);

            sender.sendMessage(new ComponentBuilder("Old Force Status: " + monitor.getForceState()).color(ChatColor.LIGHT_PURPLE).create());

            monitor.setForceState(newState);
        }

        sender.sendMessage(new ComponentBuilder("Current Force Status: " + monitor.getForceState()).color(ChatColor.GOLD).create());
        sender.sendMessage(new ComponentBuilder("Current Session Status: " + monitor.getDiscoveredState()).color(ChatColor.GOLD).create());
    }

    private static @Nullable SessionState parseSessionStateCommand(String command) {
        switch (command) {
        case "on":
            return SessionState.ONLINE;
        case "off":
            return SessionState.OFFLINE;
        case "clear":
        case "none":
            return SessionState.ABSENT;
        default:
            return null;
        }
    }
}
