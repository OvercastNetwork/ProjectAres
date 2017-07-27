package tc.oc.api.minecraft.queue;

import javax.inject.Inject;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.NestedCommand;
import tc.oc.api.message.types.Ping;
import tc.oc.api.message.types.Reply;
import tc.oc.commons.core.commands.CommandFutureCallback;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.commands.NestedCommands;
import tc.oc.minecraft.scheduler.SyncExecutor;
import tc.oc.api.queue.Exchange;
import tc.oc.api.queue.Transaction;
import tc.oc.api.util.Permissions;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.Audiences;
import tc.oc.minecraft.api.command.CommandSender;

/**
 * AMQP debugging commands
 */
class QueueCommands implements NestedCommands {
    public static class Parent implements Commands {
        @Command(
            aliases = "amqp",
            desc = "AMQP testing commands",
            min = 1,
            max = -1
        )
        @CommandPermissions(Permissions.DEVELOPER)
        @NestedCommand(QueueCommands.class)
        public void amqp(CommandContext args, CommandSender sender) throws CommandException {}
    }

    private final Exchange.Direct exchange;
    private final Transaction.Factory transactions;
    private final SyncExecutor syncExecutor;
    private final Audiences audiences;

    @Inject QueueCommands(Exchange.Direct exchange, Transaction.Factory transactions, SyncExecutor syncExecutor, Audiences audiences) {
        this.exchange = exchange;
        this.transactions = transactions;
        this.syncExecutor = syncExecutor;
        this.audiences = audiences;
    }

    @Command(
        aliases = {"ping"},
        desc = "Send a Ping message to the direct exchange, -r to wait for a reply",
        usage = "<routing key> [-s | -f | -e]",
        flags = "sfe",
        min = 1,
        max = 1
    )
    public void ping(CommandContext args, CommandSender sender) throws CommandException {
        final String routingKey = args.getString(0);
        final Audience audience = audiences.get(sender);

        audience.sendMessage("ping " + routingKey);

        final Ping.ReplyWith replyWith;
        if(args.hasFlag('s')) {
            replyWith = Ping.ReplyWith.success;
        } else if(args.hasFlag('f')) {
            replyWith = Ping.ReplyWith.failure;
        } else if(args.hasFlag('e')) {
            replyWith = Ping.ReplyWith.exception;
        } else {
            replyWith = null;
        }

        if(replyWith == null) {
            exchange.publishAsync(new Ping(), routingKey);
        } else {
            final Transaction<Reply> transaction = transactions.request(new Ping(replyWith), routingKey);
            syncExecutor.callback(
                transaction,
                CommandFutureCallback.onSuccess(sender, args, reply -> {
                    audience.sendMessage(reply.getClass().getSimpleName() + String.format(" (%.3fms)", transaction.elapsedTimeMillis()));
                })
            );
        }
    }
}
