package tc.oc.pgm.modes;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.NestedCommand;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.chat.HeaderComponent;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.commands.NestedCommands;
import tc.oc.commons.core.commands.TranslatableCommandException;
import tc.oc.commons.core.formatting.PeriodFormats;
import tc.oc.commons.core.util.Pair;
import tc.oc.pgm.commands.CommandUtils;

@Singleton
public class ObjectiveModeCommands implements NestedCommands {
    public static final String ADVANCE_PERMISSION = "pgm.modes.advance";

    @Singleton
    public static class Parent implements Commands {

        @Inject ObjectiveModeCommands objectiveModeCommands;

        @Command(
            aliases = {"mode", "modes"},
            desc = "General mode commands",
            min = 0,
            max = -1
        )
        @NestedCommand(value = ObjectiveModeCommands.class, executeBody = true)
        public void modes(CommandContext args, CommandSender sender) throws CommandException {
            objectiveModeCommands.list(args, sender);
        }
    }

    private final Provider<ObjectiveModeManager> manager;
    private final Audiences audiences;

    @Inject ObjectiveModeCommands(Provider<ObjectiveModeManager> manager, Audiences audiences) {
        this.manager = manager;
        this.audiences = audiences;
    }

    private void sendNoModes(Audience audience) {
        audience.sendMessage(new TranslatableComponent("match.objectiveMode.noModes"));
    }

    private BaseComponent formatMode(ObjectiveMode mode, Duration time) {
        return new Component(
            new TranslatableComponent(
                "match.objectiveMode.countdown",
                new Component(manager.get().name(mode), ChatColor.GOLD),
                new Component(PeriodFormats.formatColons(time), ChatColor.AQUA)
            ),
            ChatColor.DARK_AQUA
        ).strikethrough(time.isZero());
    }

    @Command(
        aliases = "next",
        flags = "f",
        desc = "Shows information about the next mode, or advance to the next mode with -f"
    )
    public void next(CommandContext args, CommandSender sender) throws CommandException {
        final ObjectiveModeManager manager = this.manager.get();
        final Audience audience = audiences.get(sender);
        final Optional<Pair<ObjectiveMode, Duration>> next = manager.nextMode();

        if(!next.isPresent()) sendNoModes(audience);

        if(args.hasFlag('f') && sender.hasPermission(ADVANCE_PERMISSION)) {
            if(!CommandUtils.getMatch(sender).hasStarted()) {
                throw new TranslatableCommandException("command.modes.advance.matchNotStarted");
            }
            manager.advance(next.get().first);
            audience.sendMessage(new Component(ChatColor.GREEN).translate("command.modes.advanced", new Component(manager.name(next.get().first)).bold(true)));
        } else {
            audience.sendMessage(formatMode(next.get().first, next.get().second));
        }
    }

    @Command(
        aliases = "list",
        desc = "Lists all modes"
    )
    public void list(CommandContext args, CommandSender sender) throws CommandException {
        final ObjectiveModeManager manager = this.manager.get();
        final Audience audience = audiences.get(sender);
        final Map<ObjectiveMode, Duration> modes = manager.modes();

        if(modes.isEmpty()) {
            sendNoModes(audience);
        } else {
            audience.sendMessage(new HeaderComponent(new TranslatableComponent("match.objectiveMode.header")));
            modes.forEach((mode, time) -> audience.sendMessage(formatMode(mode, time)));
        }
    }
}
