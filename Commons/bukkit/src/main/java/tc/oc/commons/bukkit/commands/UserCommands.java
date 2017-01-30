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
import tc.oc.api.docs.PlayerId;
import tc.oc.api.minecraft.MinecraftService;
import tc.oc.minecraft.scheduler.SyncExecutor;
import tc.oc.api.sessions.SessionService;
import tc.oc.commons.bukkit.chat.BukkitAudiences;
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
    private final UserFinder userFinder;
    private final IdentityProvider identityProvider;
    private final UserFormatter userFormatter;

    @Inject UserCommands(MinecraftService minecraftService, SyncExecutor syncExecutor, SessionService sessionService, UserFinder userFinder, IdentityProvider identityProvider, UserFormatter userFormatter) {
        this.minecraftService = minecraftService;
        this.syncExecutor = syncExecutor;
        this.sessionService = sessionService;
        this.userFinder = userFinder;
        this.identityProvider = identityProvider;
        this.userFormatter = userFormatter;
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
        aliases = { "friends", "fr", "fs" },
        usage = "[page #]",
        desc = "Shows what servers your friends are on",
        min = 0,
        max = 1
    )
    @CommandPermissions("projectares.friends.view")
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
                final Audience audience = BukkitAudiences.getAudience(sender);
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
                userFormatter.formatSessions(result.documents()).forEach(audience::sendMessage);
            })
        );
    }
}
