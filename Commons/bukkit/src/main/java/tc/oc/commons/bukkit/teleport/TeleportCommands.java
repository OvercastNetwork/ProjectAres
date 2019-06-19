package tc.oc.commons.bukkit.teleport;

import com.google.common.util.concurrent.ListenableFuture;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import javax.inject.Inject;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.api.users.UserSearchResponse;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.commons.bukkit.chat.PlayerComponent;
import tc.oc.commons.bukkit.commands.CommandUtils;
import tc.oc.commons.bukkit.commands.UserFinder;
import tc.oc.commons.bukkit.nick.IdentityProvider;
import tc.oc.commons.core.commands.CommandFutureCallback;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.commands.TranslatableCommandException;
import tc.oc.minecraft.scheduler.SyncExecutor;

public class TeleportCommands implements Commands {

    private final SyncExecutor syncExecutor;
    private final Teleporter teleporter;
    private final UserFinder userFinder;
    private final IdentityProvider identityProvider;

    @Inject TeleportCommands(SyncExecutor syncExecutor, Teleporter teleporter, UserFinder userFinder, IdentityProvider identityProvider) {
        this.syncExecutor = syncExecutor;
        this.teleporter = teleporter;
        this.userFinder = userFinder;
        this.identityProvider = identityProvider;
    }

    @Command(
        aliases = { "remoteteleport", "rtp", "goto" },
        desc = "Teleport to a player anywhere on the network",
        usage = "[traveler] <destination>",
        min = 1,
        max = 2
    )
    public void remoteTeleport(final CommandContext args, final CommandSender sender) throws CommandException {
        final Player traveler;
        final ListenableFuture<UserSearchResponse> future;

        if(args.argsLength() >= 2) {
            CommandUtils.assertPermission(sender, Teleporter.PERMISSION_OTHERS);
            traveler = CommandUtils.findOnlinePlayer(args, sender, 0);
            future = userFinder.findUser(sender, args, 1);
        } else {
            CommandUtils.assertPermission(sender, Teleporter.PERMISSION);
            traveler = CommandUtils.senderToPlayer(sender);
            future = userFinder.findUser(sender, args, 0);
        }

        syncExecutor.callback(
            future,
            CommandFutureCallback.onSuccess(sender, args, result -> {
                final PlayerComponent playerComponent = new PlayerComponent(identityProvider.createIdentity(result), NameStyle.FANCY);

                if(!result.online) {
                    throw new TranslatableCommandException("command.playerNotOnline", playerComponent);
                } else if(result.last_server == null) {
                    // Probably because player has disabled "show current server"
                    throw new TranslatableCommandException("command.playerLocationUnavailable", playerComponent);
                } else {
                    teleporter.remoteTeleport(traveler, result.last_server, result.user.uuid());
                }
            })
        );
    }
}
