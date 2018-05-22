package tc.oc.commons.bukkit.commands;

import javax.inject.Inject;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.CommandSender;
import tc.oc.api.bukkit.users.Users;
import tc.oc.api.docs.Friendship;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.User;
import tc.oc.api.friendships.FriendshipRequest;
import tc.oc.api.friendships.FriendshipService;
import tc.oc.api.minecraft.MinecraftService;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.chat.Links;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.commons.bukkit.chat.PlayerComponent;
import tc.oc.commons.core.util.Lazy;
import tc.oc.minecraft.scheduler.SyncExecutor;
import tc.oc.api.sessions.SessionService;
import tc.oc.commons.bukkit.chat.ComponentPaginator;
import tc.oc.commons.bukkit.chat.ComponentRenderers;
import tc.oc.commons.bukkit.chat.HeaderComponent;
import tc.oc.commons.bukkit.format.UserFormatter;
import tc.oc.commons.bukkit.nick.IdentityProvider;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.commands.CommandFutureCallback;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.commands.TranslatableCommandException;

/**
 * Commands for querying and possibly manipulating user records
 */
public class UserCommands implements Commands {

    private final MinecraftService minecraftService;
    private final SyncExecutor syncExecutor;
    private final SessionService sessionService;
    private final FriendshipService friendshipService;
    private final UserFinder userFinder;
    private final IdentityProvider identityProvider;
    private final UserFormatter userFormatter;
    private final Audiences audiences;

    @Inject UserCommands(MinecraftService minecraftService, SyncExecutor syncExecutor, SessionService sessionService, FriendshipService friendshipService, UserFinder userFinder, IdentityProvider identityProvider, UserFormatter userFormatter, Audiences audiences) {
        this.minecraftService = minecraftService;
        this.syncExecutor = syncExecutor;
        this.sessionService = sessionService;
        this.friendshipService = friendshipService;
        this.userFinder = userFinder;
        this.identityProvider = identityProvider;
        this.userFormatter = userFormatter;
        this.audiences = audiences;
    }

    @Command(
        aliases = { "seen", "find" },
        usage = "<player>",
        desc = "Shows when a player was last seen",
        min = 1,
        max = 1
    )
    @CommandPermissions("projectares.seen")
    public void find(final CommandContext args, final CommandSender sender) throws CommandException {
        syncExecutor.callback(
            userFinder.findUser(sender, args, 0),
            CommandFutureCallback.onSuccess(sender, args, result -> {
                ComponentRenderers.send(sender, userFormatter.formatLastSeen(result));
            })
        );
    }

    @Command(
        aliases = { "friends", "frs" },
        usage = "[page #]",
        desc = "Shows what servers your friends are on",
        min = 0,
        max = 1
    )
    @CommandPermissions("ocn.friend.list")
    public void friends(final CommandContext args, final CommandSender sender) throws CommandException {
        final PlayerId playerId = Users.playerId(CommandUtils.senderToPlayer(sender));
        final int page = args.getInteger(0, 1);

        syncExecutor.callback(
            sessionService.friends(playerId),
            CommandFutureCallback.onSuccess(sender, args, result -> {
                if(result.documents().isEmpty()) {
                    throw new TranslatableCommandException("command.friends.none");
                }

                new ComponentPaginator() {
                    @Override protected BaseComponent title() {
                        return new TranslatableComponent("command.friends.title");
                    }
                }.display(sender, userFormatter.formatSessions(result.documents()), page);
            })
        );
    }

    @Command(
        aliases = { "friend", "fr" },
        usage = "<player>",
        desc = "Send a friend request to a player",
        min = 1,
        max = 1
    )
    @CommandPermissions("ocn.friend.request")
    public void friend(final CommandContext args, final CommandSender sender) throws CommandException {
        User friender = userFinder.getLocalUser(CommandUtils.senderToPlayer(sender));
        Audience audience = audiences.get(sender);
        syncExecutor.callback(
            userFinder.findUser(sender, args, 0),
            response -> {
                Lazy<PlayerComponent> friended = Lazy.from(
                    () -> new PlayerComponent(identityProvider.currentIdentity(response.user))
                );
                if(response.disguised) {
                    // If player is disguised pretend they do not accept friends
                    audience.sendWarning(new TranslatableComponent(
                        "friend.request.not_accepting",
                        friended.get()
                    ), false);
                } else {
                    syncExecutor.callback(
                        friendshipService.create(FriendshipRequest.create(
                            friender.player_id(),
                            response.user.player_id()
                        )),
                        response1 -> {
                            if(response1.success()) {
                                Friendship friendship = response1.friendships().get(0);
                                audience.sendMessage(new TranslatableComponent(
                                    "friend.request." + (friendship.accepted() ? "accepted" : "sent"),
                                    friended.get()
                                ));
                            } else {
                                audience.sendWarning(new TranslatableComponent(
                                    "friend.request." + response1.error(),
                                    friended.get(),
                                    Links.shopLink(true)
                                ), false);
                            }
                        }
                    );
                }
            }
        );
    }

    @Command(
        aliases = { "unfriend", "unfr" },
        usage = "<player>",
        desc = "Withdraw a friend request or unfriend a current friend",
        min = 1,
        max = 1
    )
    @CommandPermissions("ocn.friend.request")
    public void unfriend(final CommandContext args, final CommandSender sender) throws CommandException {
        User friender = userFinder.getLocalUser(CommandUtils.senderToPlayer(sender));
        Audience audience = audiences.get(sender);
        syncExecutor.callback(
            userFinder.findUser(sender, args, 0),
            response -> {
                boolean were = friender.friends().contains(response.user);
                syncExecutor.callback(
                    friendshipService.destroy(FriendshipRequest.create(
                        friender.player_id(),
                        response.user.player_id()
                    )),
                    response1 -> {
                        PlayerComponent friended = new PlayerComponent(identityProvider.currentIdentity(response.user));
                        if(response1.success()) {
                            audience.sendMessage(new TranslatableComponent(
                                "friend.unrequest." + (were ? "success" : "withdraw"),
                                friended
                            ));
                        } else {
                            audience.sendWarning(new TranslatableComponent(
                                "friend.unrequest." + response1.error(),
                                friended
                            ), false);
                        }
                    }
                );
            }
        );
    }

    @Command(
        aliases = { "staff", "mods" },
        desc = "List staff members who are on the network right now",
        min = 0,
        max = 0
    )
    @CommandPermissions("projectares.showstaff")
    public void staff(final CommandContext args, final CommandSender sender) throws CommandException {

        syncExecutor.callback(
            sessionService.staff(minecraftService.getLocalServer().network(), identityProvider.revealAll(sender)),
            CommandFutureCallback.onSuccess(sender, args, result -> {
                final Audience audience = audiences.get(sender);
                if(result.documents().isEmpty()) {
                    audience.sendMessage(new TranslatableComponent("command.staff.noStaffOnline"));
                    return;
                }

                audience.sendMessage(new HeaderComponent(
                    new Component(ChatColor.GRAY)
                        .extra(new Component(new TranslatableComponent("command.staff.title"), ChatColor.BLUE))
                        .extra(new Component(" ("))
                        .extra(new Component(String.valueOf(result.documents().size()), ChatColor.AQUA))
                        .extra(")")
                ));
                userFormatter.formatSessions(result.documents(), NameStyle.COLOR).forEach(audience::sendMessage);
            })
        );
    }
}
