package tc.oc.commons.bukkit.whisper;

import javax.inject.Inject;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.docs.User;
import tc.oc.api.docs.Whisper;
import tc.oc.api.exceptions.NotFound;
import tc.oc.api.whispers.WhisperService;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.commands.CommandUtils;
import tc.oc.commons.bukkit.commands.UserFinder;
import tc.oc.commons.bukkit.nick.Identity;
import tc.oc.commons.bukkit.nick.IdentityProvider;
import tc.oc.minecraft.scheduler.MainThreadExecutor;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.commands.CommandFutureCallback;
import tc.oc.commons.core.commands.Commands;

public class WhisperCommands implements Commands {
    private final BukkitUserStore userStore;
    private final IdentityProvider identityProvider;
    private final UserFinder userFinder;
    private final MainThreadExecutor executor;
    private final Audiences audiences;
    private final WhisperService whisperService;
    private final WhisperSender whisperSender;
    private final WhisperFormatter formatter;

    @Inject WhisperCommands(BukkitUserStore userStore,
                            IdentityProvider identityProvider,
                            UserFinder userFinder,
                            MainThreadExecutor executor,
                            Audiences audiences,
                            WhisperSender whisperSender,
                            WhisperService whisperService,
                            WhisperFormatter formatter) {
        this.userStore = userStore;
        this.identityProvider = identityProvider;
        this.userFinder = userFinder;
        this.executor = executor;
        this.audiences = audiences;
        this.whisperSender = whisperSender;
        this.whisperService = whisperService;
        this.formatter = formatter;
    }

    @Command(
        aliases = {"msg", "message", "whisper", "pm", "tell", "dm"},
        usage = "<target> <message...>",
        desc = "Private message a user",
        min = 2,
        max = -1
    )
    @CommandPermissions("projectares.msg")
    public void message(final CommandContext args, final CommandSender sender) throws CommandException {
        final Player player = CommandUtils.senderToPlayer(sender);
        final Identity from = identityProvider.currentIdentity(player);
        final String content = args.getJoinedStrings(1);

        executor.callback(
            userFinder.findUser(sender, args, 0),
            CommandFutureCallback.onSuccess(sender, args, response -> {
                whisperSender.send(sender, from, identityProvider.createIdentity(response), content);
            })
        );
    }

    @Command(
        aliases = {"reply", "r"},
        usage = "<message...>",
        desc = "Reply to last user",
        min = 1,
        max = -1
    )
    @CommandPermissions("projectares.msg")
    public void reply(final CommandContext args, final CommandSender sender) throws CommandException {
        final Player player = CommandUtils.senderToPlayer(sender);
        final User user = userStore.getUser(player);
        final Audience audience = audiences.get(sender);
        final String content = args.getJoinedStrings(0);

        executor.callback(
            whisperService.forReply(user),
            CommandFutureCallback.<Whisper>onSuccess(sender, args, original -> {
                final Identity from, to;
                if(user.equals(original.sender_uid())) {
                    // Follow-up of previously sent message
                    from = formatter.senderIdentity(original);
                    to = formatter.recipientIdentity(original);
                } else {
                    // Reply to received message
                    from = formatter.recipientIdentity(original);
                    to = formatter.senderIdentity(original);
                }
                whisperSender.send(sender, from, to, content);
            }).onFailure(NotFound.class, e -> formatter.noReply(sender))
        );
    }
}
