package tc.oc.pgm.destroyable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import tc.oc.commons.core.commands.Commands;
import tc.oc.pgm.commands.CommandUtils;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.inject.MatchScoped;

import static tc.oc.commons.core.exception.LambdaExceptionUtils.rethrowSupplier;

@MatchScoped
public class DestroyableCommands implements Commands {

    private final Optional<DestroyableMatchModule> module;

    @Inject private DestroyableCommands(Optional<DestroyableMatchModule> module) {
        this.module = module;
    }

    private DestroyableMatchModule matchModule() throws CommandException {
        return module.orElseThrow(() -> new CommandException("No destroyables"));
    }

    private Destroyable lookup(Match match, String slug) throws CommandException {
        return match.features()
                    .bySlug(Destroyable.class, slug)
                    .orElseGet(rethrowSupplier(() -> {
                        final List<Destroyable> destroyables = match.features()
                                                                    .all(Destroyable.class)
                                                                    .filter(d -> d.slug().startsWith(slug))
                                                                    .collect(Collectors.toList());
                        switch(destroyables.size()) {
                            case 0: throw new CommandException("No destroyable with ID '" + slug + "'");
                            case 1: return destroyables.get(0);
                            default: throw new CommandException("Multiple destroyables have IDs that start with '" + slug + "'");
                        }
                    }));
    }

    private void assertEditPerms(CommandSender sender) throws CommandException {
        if(!sender.hasPermission("pgm.destroyable.edit")) {
            throw new CommandException("You don't have permission to do that");
        }
    }

    @Command(
        aliases = {"destroyable", "destroyables"},
        desc = "Commands for working with destroyables (monuments)",
        usage = "[id [completion[=(<blocks>|<percent>%)]]]",
        min = 0,
        max = -1
    )
    public void destroyable(CommandContext args, CommandSender sender) throws CommandException {
        String id = args.getString(0, null);

        if(id == null) {
            list(args, sender);
        } else {
            Destroyable destroyable = lookup(CommandUtils.getMatch(sender), id);
            String action = args.getString(1, null);

            if(action == null) {
                details(args, sender, destroyable);
            } else if(action.startsWith("completion")) {
                if(action.startsWith("completion=")) {
                    setCompletion(args, sender, destroyable, action.substring("completion=".length()));
                }
                completion(args, sender, destroyable);
            } else {
                throw new CommandException("Invalid action '" + action + "'");
            }
        }
    }

    public void list(CommandContext args, CommandSender sender) throws CommandException {
        List<String> lines = new ArrayList<>();
        lines.add(ChatColor.GRAY + "Destroyables");

        for(Destroyable destroyable : matchModule().getDestroyables()) {
            lines.add("  " + ChatColor.AQUA + destroyable.slug() +
                      ": " + ChatColor.WHITE + destroyable.getName() +
                      ChatColor.GRAY + " owned by " + destroyable.getOwner().getColoredName());
        }

        sender.sendMessage(lines.toArray(new String[lines.size()]));
    }

    private String formatProperty(String name, Object value) {
        return "  " + ChatColor.GRAY + name + ChatColor.DARK_GRAY + ": " + ChatColor.WHITE + value;
    }

    public void details(CommandContext args, CommandSender sender, Destroyable destroyable) throws CommandException {
        List<String> lines = new ArrayList<>();

        lines.add(ChatColor.GRAY + "Destroyable " + ChatColor.AQUA + destroyable.slug());
        lines.add(formatProperty("name", destroyable.getName()));
        lines.add(formatProperty("owner", destroyable.getOwner().getColoredName()));
        lines.add(formatProperty("size", destroyable.getMaxHealth()));
        lines.add(formatProperty("completion", destroyable.getBreaksRequired() + " (" + destroyable.renderDestructionRequired() + ")"));

        sender.sendMessage(lines.toArray(new String[lines.size()]));
    }

    public void completion(CommandContext args, CommandSender sender, Destroyable destroyable) throws CommandException {
        sender.sendMessage(ChatColor.GRAY + "Destroyable " +
                           ChatColor.AQUA + destroyable.slug() +
                           ChatColor.GRAY + " has completion " +
                           ChatColor.WHITE + Math.round(destroyable.getDestructionRequired() * 100) + "%" +
                           ChatColor.GRAY + " or " +
                           ChatColor.WHITE + destroyable.getBreaksRequired() + "/" + destroyable.getMaxHealth() +
                           ChatColor.GRAY + " breaks");
    }

    public void setCompletion(CommandContext args, CommandSender sender, Destroyable destroyable, String value) throws CommandException {
        assertEditPerms(sender);

        Double completion;
        Integer breaks;

        if(value.endsWith("%")) {
            completion = Double.parseDouble(value.substring(0, value.length() - 1)) / 100d;
            breaks = null;
        } else {
            completion = null;
            breaks = Integer.parseInt(value);
        }

        try {
            if(completion != null) {
                destroyable.setDestructionRequired(completion);
            }
            else if(breaks != null) {
                destroyable.setBreaksRequired(breaks);
            }
        } catch(IllegalArgumentException e) {
            throw new CommandException(e.getMessage());
        }
    }
}
