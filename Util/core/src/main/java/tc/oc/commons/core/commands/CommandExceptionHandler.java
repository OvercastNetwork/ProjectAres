package tc.oc.commons.core.commands;

import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.inject.ImplementedBy;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandNumberFormatException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.CommandUsageException;
import com.sk89q.minecraft.util.commands.MissingNestedCommandException;
import com.sk89q.minecraft.util.commands.WrappedCommandException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.Audiences;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.exception.ExceptionHandler;
import tc.oc.commons.core.exception.LoggingExceptionHandler;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.util.StackTrace;
import tc.oc.commons.core.util.Streams;
import tc.oc.minecraft.api.command.CommandSender;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Handles exceptions from a user action. This could be a text command,
 * or any other action for which a text response is appropriate.
 *
 * {@link CommandException}s with a message are displayed to the user,
 * and no other action is taken. Other exceptions are handled as
 * internal errors by logging them and sending a generic error
 * message to the user.
 */
public interface CommandExceptionHandler extends ExceptionHandler {
    @ImplementedBy(CommandExceptionHandlerFactory.class)
    interface Factory {
        /**
         * @deprecated use {@link #create(CommandSender, CommandInvocationInfo)}
         */
        @Deprecated
        CommandExceptionHandler create(Audience audience, CommandInvocationInfo command);

        CommandExceptionHandler create(CommandSender sender, CommandInvocationInfo command);

        default CommandExceptionHandler create(CommandSender sender) {
            return create(sender, CommandInvocationInfo.of(sender));
        }

        default CommandExceptionHandler create(CommandSender sender, @Nullable String command, @Nullable Iterable<String> args) {
            return create(sender, CommandInvocationInfo.of(sender, command, args));
        }
    }
}

class CommandExceptionHandlerFactory implements CommandExceptionHandler.Factory {

    private final Audiences audiences;
    private final Loggers loggers;

    @Inject CommandExceptionHandlerFactory(Audiences audiences, Loggers loggers) {
        this.audiences = audiences;
        this.loggers = loggers;
    }

    @Override
    public CommandExceptionHandler create(Audience audience, CommandInvocationInfo command) {
        return new HandlerImpl(audience, command);
    }

    @Override
    public CommandExceptionHandler create(CommandSender sender, CommandInvocationInfo command) {
        return create(audiences.get(sender), command);
    }

    class HandlerImpl implements CommandExceptionHandler {

        private final Audience senderAudience;
        private final LoggingExceptionHandler fallbackHandler;

        HandlerImpl(Audience audience, CommandInvocationInfo command) {
            this.senderAudience = checkNotNull(audience);
            checkNotNull(command);

            this.fallbackHandler = new LoggingExceptionHandler(loggers) {
                @Override
                protected String messagePrefix() {
                    return Streams.compact(Optional.of("Exception dispatching command"),
                                           command.sender().map(sender -> " from " + sender),
                                           command.commandLine().map(cmd -> ": " + cmd))
                                  .collect(Collectors.joining());
                }
            };
        }

        protected void sendError(BaseComponent message) {
            senderAudience.sendWarning(message, false);
        }

        protected void sendUsage(CommandUsageException e) {
            senderAudience.sendMessage(new Component(e.getUsage(), ChatColor.RED));
        }

        protected void sendInternalError() {
            senderAudience.sendWarning(new TranslatableComponent("command.error.internal"), false);
        }

        protected void handleInternalError(Throwable throwable, @Nullable Object source, @Nullable StackTrace trace) {
            sendInternalError();
            fallbackHandler.handleException(throwable, source, trace);
        }

        @Override
        public void handleException(Throwable throwable, @Nullable Object source, @Nullable StackTrace trace) {
            try {
                throw throwable;
            } catch (CommandPermissionsException e) {
                sendError(new TranslatableComponent("noPermissions"));
            } catch (MissingNestedCommandException e) {
                sendUsage(e);
            } catch (CommandUsageException e) {
                sendError(new Component(e.getMessage()));
                sendUsage(e);
            } catch (CommandNumberFormatException e) {
                sendError(new TranslatableComponent("invalidInput.string.numberExpected", e.getActualText()));
            } catch (WrappedCommandException e) {
                handleInternalError(e.getCause(), source, trace);
            } catch (ComponentCommandException e) {
                sendError(e.getComponentMessage());
            } catch (CommandException e) {
                if(e.getMessage() != null) {
                    sendError(new Component(e.getMessage()));
                } else {
                    handleInternalError(e, source, trace);
                }
            } catch (Throwable e) {
                handleInternalError(e, source, trace);
            }
        }
    }
}