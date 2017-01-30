package tc.oc.commons.bukkit.commands;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.sk89q.bukkit.util.CommandsManagerRegistration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import tc.oc.commons.core.commands.CommandRegistryImpl;
import tc.oc.commons.core.commands.CommandRegistry;
import tc.oc.commons.core.plugin.PluginScoped;
import tc.oc.minecraft.api.event.Enableable;

@PluginScoped
public class BukkitCommandRegistry extends CommandRegistryImpl<CommandSender> implements CommandRegistry, TabExecutor, Enableable {

    // The Minecraft client gets confused by spaces in tab completion results,
    // so we replace them with this special character, and do the inverse
    // replacement for all incoming command text.
    private static final char FAKE_SPACE_CHAR = '\u2508';

    private final @Nullable CommandsManagerRegistration registrator;

    @Inject BukkitCommandRegistry(Plugin plugin) {
        // Don't register any commands if this plugin is disabled
        this.registrator = plugin.isActive() ? new CommandsManagerRegistration(plugin, this, this, this.commandsManager)
                                             : null;
    }

    /**
     * Register a class containing commands
     */
    @Override
    public  <T> void register(Class<T> clazz, @Nullable Provider<? extends T> provider) {
        if(registrator != null) {
            registrator.register(clazz, provider);
        }
    }

    @Override
    public void disable() {
        if(registrator != null) {
            registrator.unregisterCommands();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        handleCommand(sender, command.getName(), decode(args));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Save the word being completed, before and after decoding
        final String encodedWord = args[args.length - 1];
        args = decode(args);
        final String decodedWord = args[args.length - 1];

        List<String> options = handleCompletion(sender, command.getName(), args);

        if(options != null) {
            // If sender is a player, encode the completion results.
            // A single result doesn't need to be encoded, because the
            // client won't try to cycle through the results.
            if(sender instanceof Player && options.size() > 1) {
                options = encode(options);
            }

            // When completing a word with an encoded space, we have to add the
            // part that was lost from decoding back to each completion result
            // e.g. the client tries to complete "the-fen", but the command method
            // returns results for "fen", so we have to restore "the-" for each result.
            if(!encodedWord.equals(decodedWord)) {
                final String extra = encodedWord.substring(0, encodedWord.length() - decodedWord.length());
                options = Lists.transform(options, option -> extra + option);
            }
        }

        return options;
    }

    private static List<String> encode(List<String> decodedOptions) {
        List<String> escapedOptions = decodedOptions;
        for(int i = 0; i < decodedOptions.size(); i++) {
            final String decodedOption = decodedOptions.get(i);
            final String encodedOption = decodedOption.replace(' ', FAKE_SPACE_CHAR);
            if(!decodedOption.equals(encodedOption)) {
                if(escapedOptions == decodedOptions) {
                    escapedOptions = new ArrayList<>(decodedOptions);
                }
                escapedOptions.set(i, encodedOption);
            }
        }
        return escapedOptions;
    }

    private static String[] decode(String[] encodedArgs) {
        List<String> decodedArgs = null;
        for(int i = 0; i < encodedArgs.length; i++) {
            final String encodedArg = encodedArgs[i];
            if(encodedArg.indexOf(FAKE_SPACE_CHAR) != -1) {
                if(decodedArgs == null) {
                    decodedArgs = new ArrayList<>();
                    for(int j = 0; j < i; j++) {
                        decodedArgs.add(encodedArgs[j]);
                    }
                }
                Iterables.addAll(decodedArgs, Splitter.on(FAKE_SPACE_CHAR).split(encodedArg));
            } else if(decodedArgs != null) {
                decodedArgs.add(encodedArg);
            }
        }
        return decodedArgs == null ? encodedArgs
                                   : decodedArgs.toArray(new String[decodedArgs.size()]);
    }
}
