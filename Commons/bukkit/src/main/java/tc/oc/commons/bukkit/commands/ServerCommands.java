package tc.oc.commons.bukkit.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.inject.Inject;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.User;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.docs.virtual.UserDoc;
import tc.oc.api.servers.ServerStore;
import tc.oc.api.users.UserService;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.chat.Paginator;
import tc.oc.commons.bukkit.chat.WarningComponent;
import tc.oc.commons.bukkit.format.ServerFormatter;
import tc.oc.commons.bukkit.teleport.Teleporter;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.commands.TranslatableCommandException;
import tc.oc.commons.core.concurrent.Flexecutor;
import tc.oc.commons.core.formatting.StringUtils;
import tc.oc.commons.core.util.Pair;
import tc.oc.minecraft.scheduler.Sync;

public class ServerCommands implements Commands {

    private final Flexecutor flexecutor;
    private final Server localServer;
    private final ServerStore serverStore;
    private final ServerFormatter formatter = ServerFormatter.light;
    private final Teleporter teleporter;
    private final BukkitUserStore userStore;
    private final UserService userService;
    private final Audiences audiences;

    private static final Comparator<Server> FULLNESS = Comparator.comparing(Server::num_online).reversed();

    @Inject ServerCommands(@Sync Flexecutor flexecutor, Server localServer, ServerStore serverStore, Teleporter teleporter, BukkitUserStore userStore, UserService userService, Audiences audiences) {
        this.flexecutor = flexecutor;
        this.localServer = localServer;
        this.serverStore = serverStore;
        this.teleporter = teleporter;
        this.userStore = userStore;
        this.userService = userService;
        this.audiences = audiences;
    }

    private BaseComponent format(Server server) {
        final Component c = new Component(formatter.nameWithDatacenter(server))
            .extra(" ")
            .extra(formatter.playerCounts(server, true));
        if(server.role() == ServerDoc.Role.PGM && server.current_match() != null) {
            c.extra(" ")
             .extra(formatter.matchTime(server.current_match()))
             .extra(" ")
             .extra(new Component(server.current_match().map().name(), ChatColor.AQUA));
        }
        return c;
    }

    @Command(
        aliases = { "servers", "srvs" },
        desc = "Show a listing of all servers on the network",
        usage = "[page]",
        flags = "a",
        min = 0,
        max = 1
    )
    public void servers(final CommandContext args, final CommandSender sender) throws CommandException {
        final List<Server> servers = new ArrayList<>(serverStore.subset(args.hasFlag('a') && sender.hasPermission("ocn.see-all-servers") ? teleporter::isConnectable : teleporter::isVisible));
        Collections.sort(servers, FULLNESS);

        new Paginator<Server>() {
            @Override
            protected BaseComponent title() {
                return new TranslatableComponent("command.servers.title");
            }

            @Override
            protected BaseComponent entry(Server server, int index) {
                return format(server);
            }
        }.display(sender, servers, args.getInteger(0, 1));
    }

    @Command(
        aliases = { "server", "srv" },
        desc = "Show the name of this server or connect to a different server",
        usage = "[-d datacenter] [name]",
        flags = "bd:"
    )
    public List<String> server(CommandContext args, final CommandSender sender) throws CommandException {
        Pair<Server, List<String>> response = find(args, sender, user -> teleporter.showCurrentServer(sender));
        if(response != null) {
            Server route = response.first;
            List<String> suggestions = response.second;
            if(suggestions != null) {
                return suggestions;
            } else if(route != null) {
                if(route.equals(localServer)) {
                    teleporter.showCurrentServer(sender);
                } else {
                    teleporter.remoteTeleport(CommandUtils.senderToPlayer(sender), route);
                }
            } else {
                teleporter.sendToLobby(CommandUtils.senderToPlayer(sender), false);
            }
        }
        return null;
    }

    @Command(
        aliases = { "default-server", "def-srv" },
        desc = "Set your default server when connecting to the network",
        usage = "[-d datacenter] [name]",
        flags = "bd:"
    )
    @CommandPermissions("ocn.default-server")
    public List<String> defaultServer(final CommandContext args, CommandSender sender) throws CommandException {
        Pair<Server, List<String>> response = find(args, sender, user -> {
            audiences.get(sender).sendMessage(
                new Component(
                    new TranslatableComponent(
                        "command.server.defaultServer.get",
                        serverStore.tryId(user.default_server_id())
                                   .map(server -> new Component(formatter.nameWithDatacenter(server)))
                                   .orElse(new Component("Automatic", ChatColor.GREEN))
                    ), ChatColor.DARK_PURPLE
                )
            );
        });
        if(response != null) {
            Server route = response.first;
            List<String> suggestions = response.second;
            if(suggestions != null) {
                return suggestions;
            } else if(route != null) {
                flexecutor.callback(
                    userService.update(
                        userStore.playerId(CommandUtils.senderToPlayer(sender)),
                        (UserDoc.DefaultServer) route::_id
                    ), user -> {
                        audiences.get(sender).sendMessage(
                            new Component(
                                new TranslatableComponent(
                                    "command.server.defaultServer.set",
                                    new Component(formatter.nameWithDatacenter(route))
                                ), ChatColor.DARK_PURPLE
                            )
                        );
                    }
                );
            } else {
                teleporter.showCurrentServer(sender);
            }
        }
        return null;
    }

    public Pair<Server, List<String>> find(CommandContext args, CommandSender sender, Consumer<User> show) throws CommandException {
        if(args.getSuggestionContext() != null) {
            return Pair.create(null, StringUtils.complete(args.getJoinedStrings(0),
                                        serverStore.all()
                                                   .filter(teleporter::isConnectable)
                                                   .map(Server::name)));
        }

        // Return current server
        if(args.argsLength() == 0) {
            if(sender instanceof Player) {
                show.accept(userStore.getUser((Player) sender));
            }
            return null;
        }

        final Player player = CommandUtils.senderToPlayer(sender);

        // Search by bungee_name
        if(args.hasFlag('b')) {
            final Server byBungee = serverStore.tryBungeeName(args.getJoinedStrings(0));
            if(byBungee == null) {
                throw new TranslatableCommandException("command.serverNotFound");
            }
            return Pair.create(byBungee, null);
        }

        // Search by name/datacenter
        // If they don't have the X-DC perm, parse the first arg as part of the
        // server name, so normal players can do "/server ghost squadron"
        final String datacenter, name;
        if(args.argsLength() > 1 && !args.hasFlag('d') && player.hasPermission(Teleporter.CROSS_DATACENTER_PERMISSION)) {
            datacenter = args.getString(0).toUpperCase();
            name = args.getJoinedStrings(1);
        } else {
            datacenter = args.getFlag('d', localServer.datacenter());
            name = args.getJoinedStrings(0);
        }

        // Special aliases for the lobby
        if(name.equals("lobby") || name.equals("hub")) {
            return Pair.create(null, null);
        }

        final Set<Server> connectable = serverStore.subset(teleporter::isConnectable);
        final List<Server> partial = new ArrayList<>();
        for(Server server : connectable) {
            if(server.name().equalsIgnoreCase(name)) {
                return Pair.create(server, null);
            }
            if(StringUtils.startsWithIgnoreCase(server.name(), name)) {
                partial.add(server);
            }
        }

        final Audience audience = audiences.get(sender);
        audience.sendMessage(new WarningComponent("command.serverNotFound"));

        // If there were no more than 3 partial matches, show them
        if(partial.size() <= 3) {
            Collections.sort(partial, FULLNESS);
            for(Server server : partial) {
                audience.sendMessage(format(server));
            }
        }
        return null;
    }

    @Command(
        aliases = {"hub", "lobby"},
        desc = "Teleport to the lobby",
        min = 0,
        max = 1
    )
    public void hub(final CommandContext args, CommandSender sender) throws CommandException {
        teleporter.sendToLobby(CommandUtils.senderToPlayer(sender), args.getString(0, null));
    }
}
