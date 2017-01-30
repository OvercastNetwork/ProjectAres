package tc.oc.commons.bukkit.commands;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.NestedCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.event.AsyncClientConnectEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import tc.oc.api.bukkit.users.OnlinePlayers;
import tc.oc.api.util.Permissions;
import tc.oc.commons.bukkit.util.PacketTracer;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.commands.NestedCommands;

import static tc.oc.commons.bukkit.commands.CommandUtils.getCommandSenderOrSelf;

/**
 * Packet tracing commands
 */
public class TraceCommands implements NestedCommands, Listener {

    private final OnlinePlayers onlinePlayers;
    private final AtomicBoolean traceAll = new AtomicBoolean(false);

    @Inject TraceCommands(OnlinePlayers onlinePlayers) {
        this.onlinePlayers = onlinePlayers;
    }

    @EventHandler
    void onLogin(AsyncClientConnectEvent event) {
        if(traceAll.get()) {
            PacketTracer.start(event.channel(), event.channel().remoteAddress().toString(), Bukkit.getLogger());
        }
    }

    public static class Parent implements Commands {
        @Command(
            aliases = "trace",
            desc = "Packet dumping commands",
            min = 1,
            max = -1
        )
        @CommandPermissions(Permissions.DEVELOPER)
        @NestedCommand(TraceCommands.class)
        public void trace(CommandContext args, CommandSender sender) throws CommandException {}
    }

    @Command(
        aliases = {"on", "start"},
        desc = "Start logging packets",
        min = 0,
        max = 1
    )
    public void start(CommandContext args, CommandSender sender) throws CommandException {
        if(sender instanceof Player || args.argsLength() >= 1) {
            final Player player = (Player) getCommandSenderOrSelf(args, sender, 0);
            if(PacketTracer.start(player, Bukkit.getLogger())) {
                sender.sendMessage("Started packet trace for " + player.getName(sender));
            }
        } else if(traceAll.compareAndSet(false, true)) {
            onlinePlayers.all().forEach(player -> PacketTracer.start(player, Bukkit.getLogger()));
            sender.sendMessage("Started global packet trace");
        }
    }

    @Command(
        aliases = {"off", "stop"},
        desc = "Stop logging packets",
        min = 0,
        max = 1
    )
    public void stop(CommandContext args, CommandSender sender) throws CommandException {
        if(sender instanceof Player || args.argsLength() >= 1) {
            final Player player = (Player) getCommandSenderOrSelf(args, sender, 0);
            if(PacketTracer.stop(player)) {
                sender.sendMessage("Stopped packet trace for " + player.getName(sender));
            }
        } else {
            traceAll.set(false);
            if(onlinePlayers.all().stream().anyMatch(PacketTracer::stop)) {
                sender.sendMessage("Stopped all packet tracing");
            }
        }
    }

    @Command(
        aliases = {"clear", "cl"},
        desc = "Clear all filters",
        min = 0,
        max = -1
    )
    public void clear(CommandContext args, CommandSender sender) throws CommandException {
        PacketTracer.clearFilter();
        sender.sendMessage("Trace filters cleared");
    }

    @Command(
        aliases = {"include", "inc"},
        desc = "Include packets in the trace",
        min = 0,
        max = -1
    )
    public void include(CommandContext args, CommandSender sender) throws CommandException {
        filter(args, sender, true);
    }

    @Command(
        aliases = {"exclude", "ex"},
        desc = "Exclude packets from the trace",
        min = 0,
        max = -1
    )
    public void exclude(CommandContext args, CommandSender sender) throws CommandException {
        filter(args, sender, false);
    }

    private static void filter(CommandContext args, CommandSender sender, boolean include) throws CommandException {
        if(PacketTracer.getDefaultInclude() == include) {
            PacketTracer.clearFilter();
            PacketTracer.setDefaultInclude(!include);
        }

        for(String name : args.getSlice(1)) {
            Class<?> type = PacketTracer.findPacketType(name);
            if(type == null) {
                throw new CommandException("No packet named '" + name + "'");
            }
            PacketTracer.filter(type, include);
            sender.sendMessage((include ? "Including" : "Excluding") + " packet " + type.getSimpleName());
        }
    }
}
