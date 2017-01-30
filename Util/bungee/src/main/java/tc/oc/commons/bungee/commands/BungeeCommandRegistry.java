package tc.oc.commons.bungee.commands;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import com.sk89q.bungee.util.CommandRegistration;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.CommandBypassException;
import net.md_5.bungee.api.plugin.Plugin;
import tc.oc.commons.core.commands.CommandRegistryImpl;
import tc.oc.commons.core.commands.CommandInvocationInfo;
import tc.oc.commons.core.commands.CommandRegistry;
import tc.oc.commons.core.plugin.PluginScoped;

@PluginScoped
public class BungeeCommandRegistry extends CommandRegistryImpl<CommandSender> implements CommandRegistry, com.sk89q.bungee.util.CommandExecutor<CommandSender> {

    private final @Nullable CommandRegistration registration;

    @Inject BungeeCommandRegistry(Plugin plugin) {
        // Don't register any commands if this plugin is disabled
        this.registration = plugin.isActive() ? new CommandRegistration(plugin, plugin.getProxy().getPluginManager(), commandsManager, this)
                                              : null;
    }

    @Override
    public <T> void register(Class<T> clazz, @Nullable Provider<? extends T> provider) {
        if(registration != null) {
            registration.register(clazz, provider);
        }
    }

    @Override
    public void onCommand(CommandSender sender, String commandName, String[] args) {
        handleCommand(sender, commandName, args);
    }

    @Override
    protected void handleException(CommandSender sender, CommandInvocationInfo command, Throwable throwable) {
        if(throwable instanceof CommandBypassException) {
            // Propagate this so Bungee can do the actual bypass
            throw (CommandBypassException) throwable;
        } else {
            super.handleException(sender, command, throwable);
        }
    }
}
