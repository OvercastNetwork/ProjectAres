package tc.oc.commons.bukkit.channels;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import tc.oc.api.docs.virtual.ChatDoc;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.commands.CommandUtils;
import tc.oc.commons.bukkit.util.SyncPlayerExecutorFactory;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.commands.TranslatableCommandException;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ChannelCommands implements Commands, Listener {

    private final SyncPlayerExecutorFactory syncPlayerExecutorFactory;
    private final ChannelRouter channelRouter;
    private final Audiences audiences;

    @Inject ChannelCommands(SyncPlayerExecutorFactory syncPlayerExecutorFactory, ChannelRouter channelRouter, Audiences audiences) {
        this.syncPlayerExecutorFactory = syncPlayerExecutorFactory;
        this.channelRouter = channelRouter;
        this.audiences = audiences;
    }

    @Command(
        aliases = "a",
        desc = "Send a message to the staff channel.",
        usage = "[message...]"
    )
    public void admin(final CommandContext args, final CommandSender sender) throws CommandException {
        onChatCommand(ChatDoc.Type.ADMIN, args, sender);
    }

    @Command(
        aliases = "g",
        desc = "Send a message to everyone on the local server.",
        usage = "[message...]"
    )
    public void server(final CommandContext args, final CommandSender sender) throws CommandException {
        onChatCommand(ChatDoc.Type.SERVER, args, sender);
    }

    @Command(
        aliases = "t",
        desc = "Send a message to your teammates.",
        usage = "[message...]"
    )
    public void chat(final CommandContext args, final CommandSender sender) throws CommandException {
        onChatCommand(ChatDoc.Type.TEAM, args, sender);
    }

    public void onChatCommand(ChatDoc.Type type, CommandContext args, CommandSender sender) throws CommandException {
        final String typeName = type.name().toLowerCase();
        final Channel channel = channelRouter.getChannel(sender, type)
                                             .orElseThrow(() -> new TranslatableCommandException("channels.unavailable", typeName));
        if(channel.sendable(sender)) {
            if(args.argsLength() == 0) {
                final Player player = CommandUtils.senderToPlayer(sender);
                if(channel.equals(channelRouter.getDefaultChannel(player))) {
                    throw new TranslatableCommandException("channels.default.alreadySet", typeName);
                } else {
                    channelRouter.setDefaultChannel(player, channel.type());
                    audiences.get(player).sendMessage(new TranslatableComponent("channels.default.set", typeName));
                }
            } else {
                channel.chat(sender, args.getRemainingString(0));
            }
        } else {
            throw new CommandPermissionsException();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        syncPlayerExecutorFactory.queued(event.getPlayer()).execute(player -> {
            Channel channel = channelRouter.getDefaultChannel(player);
            if(!channel.sendable(player)) {
                // If player cannot chat in their preferred channel,
                // assume they can send to the default channel.
                channel = channelRouter.getDefaultChannel();
            }
            channel.chat(player, event.getMessage());
        });
    }

}
