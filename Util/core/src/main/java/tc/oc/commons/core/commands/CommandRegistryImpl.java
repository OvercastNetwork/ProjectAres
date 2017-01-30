package tc.oc.commons.core.commands;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import com.sk89q.minecraft.util.commands.CommandsManager;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.minecraft.api.command.CommandSender;

/**
 * Base for the cross-platform command system.
 */
public abstract class CommandRegistryImpl<Sender extends CommandSender> implements CommandRegistry {

    protected Logger logger;
    @Inject private CommandExceptionHandler.Factory exceptionHandlerFactory;

    protected final CommandsManager<Sender> commandsManager = new CommandsManager<Sender>() {
        @Override
        public boolean hasPermission(Sender player, String permission) {
            return player.hasPermission(permission);
        }
    };

    @Inject void init(Loggers loggers, GuiceInjectorAdapter injector, Set<CommandBinder.Binding> bindings) {
        logger = loggers.get(getClass());

        // Adapt the Guice Injector directly to a command-framework Injector
        commandsManager.setInjector(injector);

        // Commands registered through CommandBinder
        bindings.forEach(binding -> register(binding.type, binding.provider));
    }

    protected @Nullable List<String> handleCompletion(Sender sender, String command, String[] args) {
        return commandsManager.complete(command, args, sender, sender);
    }

    protected void handleCommand(Sender sender, String command, String[] args) {
        try {
            commandsManager.execute(command, args, sender, sender);
        } catch(Throwable e) {
            handleException(sender, CommandInvocationInfo.of(sender, command, Arrays.asList(args)), e);
        }
    }

    protected void handleException(Sender sender, CommandInvocationInfo command, Throwable throwable) {
        exceptionHandlerFactory.create(sender, command).handleException(throwable, null, null);
    }
}
