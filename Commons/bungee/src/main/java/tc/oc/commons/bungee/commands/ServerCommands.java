package tc.oc.commons.bungee.commands;

import java.util.concurrent.ExecutorService;
import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.google.common.util.concurrent.Futures;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.CommandBypassException;
import tc.oc.api.bungee.users.BungeeUserStore;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.message.types.UseServerRequest;
import tc.oc.api.message.types.UseServerResponse;
import tc.oc.api.minecraft.users.UserStore;
import tc.oc.api.model.ModelSync;
import tc.oc.api.servers.ServerService;
import tc.oc.commons.bungee.servers.ServerTracker;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.concurrent.Flexecutor;
import tc.oc.commons.core.restart.RestartManager;
import tc.oc.minecraft.scheduler.Sync;

public class ServerCommands implements Commands {

    private final RestartManager restartManager;
    private final ServerTracker serverTracker;
    private final ProxyServer proxy;
    private final ExecutorService executor;
    private final ServerService serverService;
    private final BungeeUserStore userStore;
    private final Flexecutor commandExecutor;

    @Inject ServerCommands(RestartManager restartManager, ServerTracker serverTracker, ProxyServer proxy,
                           @ModelSync ExecutorService executor, ServerService serverService,
                           BungeeUserStore userStore, @Sync Flexecutor commandExecutor) {
        this.restartManager = restartManager;
        this.serverTracker = serverTracker;
        this.proxy = proxy;
        this.executor = executor;
        this.serverService = serverService;
        this.userStore = userStore;
        this.commandExecutor = commandExecutor;
    }

    @Command(
            aliases = {"hub", "lobby"},
            desc = "Teleport to the lobby"
    )
    public void hub(final CommandContext args, CommandSender sender) throws CommandException {
        if(sender instanceof ProxiedPlayer) {
            final ProxiedPlayer player = (ProxiedPlayer) sender;
            final Server server = Futures.getUnchecked(executor.submit(() -> serverTracker.byPlayer(player)));
            if(server.role() == ServerDoc.Role.LOBBY || server.role() == ServerDoc.Role.PGM) {
                // If Bukkit server is running Commons, let it handle the command
                throw new CommandBypassException();
            }

            player.connect(proxy.getServerInfo("default"));
            player.sendMessage(new ComponentBuilder("Teleporting you to the lobby").color(ChatColor.GREEN).create());
        } else {
            sender.sendMessage(new ComponentBuilder("Only players may use this command").color(ChatColor.RED).create());
        }
    }

    @Command(
        aliases = {"requestserver", "re"},
        desc = "Request a server for yourself",
        max = 1
    )
    @CommandPermissions("ocn.requestserver")
    public void request(final CommandContext args, CommandSender sender) throws CommandException {
        if(sender instanceof ProxiedPlayer) {
            final ProxiedPlayer player = (ProxiedPlayer) sender;
            final String name = player.hasPermission("ocn.requestserver.custom") && args.argsLength() > 0 ? args.getString(0) : "";
            commandExecutor.callback(
                serverService.requestServer(new UseServerRequest() {
                    @Nonnull @Override public String user_id() {
                        return userStore.getUser(player)._id();
                    }
                    @Nonnull @Override public String server_name() {
                        return name;
                    }
                }), (response) -> {
                    if (response.now()) {
                        player.connect(proxy.getServerInfo(response.server_name()));
                        player.sendMessage(new ComponentBuilder("Your server is already online! Connecting...").color(ChatColor.GREEN).create());
                    } else {
                        player.sendMessage(new ComponentBuilder("Server requested! Please wait up to two minutes before trying to connect to /server " +
                                                                response.server_name()).color(ChatColor.GOLD).create());
                    }
                });
        } else {
            sender.sendMessage(new ComponentBuilder("Only players may use this command").color(ChatColor.RED).create());
        }
    }

    @Command(
            aliases = {"gserver"},
            desc = "Global server teleport",
            usage = "<server>",
            min = 1,
            max = 1
    )
    @CommandPermissions("bungeecord.command.server")
    public void gserver(final CommandContext args, CommandSender sender) {
        if(sender instanceof ProxiedPlayer) {
            String name = args.getString(0);
            ServerInfo info = proxy.getServerInfo(name);
            if(info != null) {
                ((ProxiedPlayer) sender).connect(info);
                sender.sendMessage(new ComponentBuilder("Teleporting you to: ").color(ChatColor.GREEN).append(name).color(ChatColor.GOLD).create());
            } else {
                sender.sendMessage(new ComponentBuilder("Invalid server: ").color(ChatColor.RED).append(name).color(ChatColor.GOLD).create());
            }
        } else {
            sender.sendMessage(new ComponentBuilder("Only players may use this command").color(ChatColor.RED).create());
        }
    }

    @Command(
            aliases = {"gqueuerestart", "gqr"},
            usage = "[-c]",
            desc = "Shutdown the next time the server is inactive and empty",
            flags = "c",
            help = "The -c flag cancels a previously queued restart"
    )
    @CommandPermissions("bungeecord.command.restart")
    public void queueRestart(final CommandContext args, final CommandSender sender) throws CommandException {
        if(restartManager == null) {
            throw new CommandException("Scheduled restarts are not enabled");
        } else {
            if(args.hasFlag('c')) {
                sender.sendMessage(new TextComponent("Restart cancelled"));
                restartManager.cancelRestart();
            } else {
                sender.sendMessage(new TextComponent("Restart queued"));
                restartManager.requestRestart("/gqr command by " + sender.getName());
            }
        }
    }
}
