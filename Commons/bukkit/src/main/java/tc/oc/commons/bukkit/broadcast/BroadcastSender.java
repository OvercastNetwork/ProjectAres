package tc.oc.commons.bukkit.broadcast;

import com.google.common.collect.Lists;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.SuggestionContext;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.docs.Chat;
import tc.oc.api.docs.Game;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.User;
import tc.oc.api.docs.virtual.ChatDoc;
import tc.oc.api.games.GameStore;
import tc.oc.api.servers.ServerStore;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.chat.BukkitSound;
import tc.oc.commons.bukkit.chat.ChatCreator;
import tc.oc.commons.bukkit.chat.PlayerComponent;
import tc.oc.commons.bukkit.chat.WarningComponent;
import tc.oc.commons.bukkit.commands.CommandUtils;
import tc.oc.commons.bukkit.nick.IdentityProvider;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.formatting.StringUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static tc.oc.api.util.Permissions.hasPermissionForEnum;
import static tc.oc.commons.bukkit.commands.CommandUtils.newCommandException;
import static tc.oc.commons.bukkit.commands.CommandUtils.tryEnum;

/**
 * Allows {@link User}s to broadcast {@link Chat} messages across multiple servers.
 */
@Singleton
public class BroadcastSender implements Commands {

    private final static String PERMISSION = "ocn.broadcast";

    private final Server server;
    private final ServerStore serverStore;
    private final GameStore gameStore;
    private final ChatCreator chatCreator;
    private final Audiences audiences;
    private final BukkitUserStore userStore;
    private final IdentityProvider identityProvider;

    @Inject BroadcastSender(Server server, ServerStore serverStore, GameStore gameStore, ChatCreator chatCreator, Audiences audiences, BukkitUserStore userStore, IdentityProvider identityProvider) {
        this.server = server;
        this.serverStore = serverStore;
        this.gameStore = gameStore;
        this.chatCreator = chatCreator;
        this.audiences = audiences;
        this.userStore = userStore;
        this.identityProvider = identityProvider;
    }

    private Set<String> destinations(@Nullable ChatDoc.Destination type) {
        Stream<String> options = Stream.empty();
        if(type != null) {
            switch(type) {
                case SERVER:
                    options = serverStore.all().map(Server::name);
                    break;
                case FAMILY:
                    options = serverStore.all().map(Server::family);
                    break;
                case GAME:
                    options = gameStore.all().map(Game::name);
                    break;
                case NETWORK:
                    options = serverStore.all().map(Server::network).map(Enum::name);
                    break;
            }
        }
        return options.map(String::toLowerCase).collect(Collectors.toSet());
    }

    @Command(
        aliases = { "broadcast", "b" },
        desc = "Broadcast a message to players across the network.",
        usage = "<destination type> <destination name> [message...]",
        min = 1
    )
    public List<String> broadcast(final CommandContext args, final CommandSender sender) throws CommandException {
        SuggestionContext suggest = args.getSuggestionContext();
        ChatDoc.Destination type = tryEnum(args.getString(0, ""), ChatDoc.Destination.class);
        Set<String> destinations = destinations(type);
        String message = "";
        String destination = "";

        if(suggest != null) {
            switch(suggest.getIndex()) {
                case 0:
                    return CommandUtils.completeEnum(args.getString(0), ChatDoc.Destination.class);
                case 1:
                    if(type != null && type != ChatDoc.Destination.GLOBAL) {
                        return StringUtils.complete(args.getString(1), destinations);
                    }
            }
        }

        if(type == null) {
            type = ChatDoc.Destination.SERVER;
            destination = server._id();
            message = args.getRemainingString(0);
        } else if(args.argsLength() >= 2) {
            if(type == ChatDoc.Destination.GLOBAL) {
                destination = null;
                message = args.getRemainingString(1);
            } else if(args.argsLength() >= 3) {
                destination = args.getString(1);
                message = args.getRemainingString(2);
                if(!destinations.contains(destination)) {
                    throw newCommandException(sender, new WarningComponent("command.error.invalidOption", destination, destinations));
                }
            }
        } else {
            CommandUtils.notEnoughArguments(sender);
        }

        if(hasPermissionForEnum(sender, PERMISSION, type)) {
            chatCreator.broadcast(
                sender instanceof Player ? userStore.tryUser((Player) sender) : null,
                message,
                type,
                destination
            );
        } else {
            throw new CommandPermissionsException();
        }
        return null;
    }

    public void show(Chat chat) {
        final Audience audience = audiences.all();
        audience.playSound(new BukkitSound(Sound.ENTITY_ENDERDRAGON_HURT, 1, 1));
        audience.sendMessage(
            new Component(
                Lists.newArrayList(
                    new Component("["),
                    new TranslatableComponent("broadcast.prefix"),
                    new Component("] "),
                    new Component(chat.message())
                ), ChatColor.RED
            ).hoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new TranslatableComponent(
                    "tip.sentBy",
                    new PlayerComponent(identityProvider.currentOrConsoleIdentity(chat.sender()))
                )
            )
        );
    }

}
