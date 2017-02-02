package tc.oc.commons.core.commands;

import java.util.Arrays;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.Audiences;
import tc.oc.commons.core.util.SystemFutureCallback;
import tc.oc.commons.core.util.ThrowingConsumer;
import tc.oc.minecraft.api.command.CommandSender;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Callback that executes as part of a player-invoked command.
 *
 * If the future task or any handler throws a {@link CommandException},
 * the error is displayed to the {@link Audience} and no exception is logged.
 *
 * Any other exception, that is not handled by a failure handler, displays
 * an "internal error" message to the {@link Audience}, in addition to
 * logging the error.
 */
public class CommandFutureCallback<T> extends SystemFutureCallback<T> {

    @Inject private static Audiences audiences;
    @Inject private static CommandExceptionHandler.Factory exceptionHandlerFactory;

    public static <T> CommandFutureCallback<T> onSuccess(CommandSender sender, ThrowingConsumer<T, ?> handler) {
        return onSuccess(sender, null, null, handler);
    }

    public static <T> CommandFutureCallback<T> onSuccess(CommandSender sender, @Nullable CommandContext command, ThrowingConsumer<T, ?> handler) {
        return onSuccess(sender,
                         CommandInvocationInfo.of(sender, command),
                         handler);
    }

    public static <T> CommandFutureCallback<T> onSuccess(CommandSender sender, @Nullable String command, @Nullable String[] args, ThrowingConsumer<T, ?> handler) {
        return new CommandFutureCallback<>(audiences.get(sender),
                                           CommandInvocationInfo.of(sender, command, args == null ? null : Arrays.asList(args)),
                                           checkNotNull(handler));
    }

    public static <T> CommandFutureCallback<T> onSuccess(CommandSender sender, CommandInvocationInfo command, ThrowingConsumer<T, ?> handler) {
        return new CommandFutureCallback<>(audiences.get(sender),
                                           command,
                                           checkNotNull(handler));
    }

    private final Audience audience;
    private final CommandInvocationInfo command;

    /**
     * @deprecated use {@link #onSuccess}
     */
    @Deprecated
    public CommandFutureCallback(Audience audience) {
        this(audience, CommandInvocationInfo.empty(), null);
    }

    private CommandFutureCallback(Audience audience, CommandInvocationInfo command, @Nullable ThrowingConsumer<T, ?> handler) {
        super(handler);
        this.audience = checkNotNull(audience);
        this.command = checkNotNull(command);
    }

    /**
     * @deprecated use {@link #onSuccess}
     */
    @Deprecated
    public void onCommandSuccess(T result) throws CommandException {}

    @Override
    public void onSuccessThrows(T result) throws Throwable {
        onCommandSuccess(result);
    }

    @Override
    protected void handleDefaultFailure(Throwable e) {
        exceptionHandlerFactory.create(audience, command)
                               .handleException(e, this, creationSite);
    }
}
