package tc.oc.pgm.mutation.command;

import java.util.Collection;
import java.util.HashSet;
import javax.inject.Inject;

import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.NestedCommand;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.CommandSender;
import tc.oc.minecraft.scheduler.SyncExecutor;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.chat.ListComponent;
import tc.oc.commons.bukkit.chat.Paginator;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.commands.NestedCommands;
import tc.oc.commons.core.formatting.StringUtils;
import tc.oc.commons.core.random.RandomUtils;
import tc.oc.pgm.commands.CommandUtils;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.mutation.Mutation;
import tc.oc.pgm.mutation.MutationMatchModule;
import tc.oc.pgm.mutation.MutationQueue;

import static tc.oc.commons.bukkit.commands.CommandUtils.newCommandException;

/**
 * Commands for {@link MutationMatchModule}.
 */
public class MutationCommands implements NestedCommands {

    public static final String PERMISSION_SET = "mutation.set";
    public static final String PERMISSION_LIST = "mutation.list";

    public static class Parent implements Commands {
        @Command(
                aliases = {"mutation", "mutations", "mutate", "mt"},
                desc = "Commands to manage match mutations.",
                usage = "<list|enable|disable>",
                min = 1,
                max = -1
        )
        @NestedCommand(value = MutationCommands.class, executeBody = true)
        public void mutate(CommandContext args, CommandSender sender) throws CommandException {}
    }

    private final SyncExecutor syncExecutor;
    private final Audiences audiences;
    private final MutationQueue mutationQueue;

    @Inject
    MutationCommands(SyncExecutor syncExecutor, Audiences audiences, MutationQueue mutationQueue) {
        this.syncExecutor = syncExecutor;
        this.audiences = audiences;
        this.mutationQueue = mutationQueue;
    }

    @Command(
            aliases = {"enable", "add"},
            desc = "Adds a mutation to the upcoming match." +
                    "You can use '?' as a wildcard or " +
                    "'*' to use all.",
            usage = "<mutation|?|*>",
            min = 1,
            max = 1
    )
    @CommandPermissions(PERMISSION_SET)
    public void enable(CommandContext args, CommandSender sender) throws CommandException {
        set(args, sender, true);
    }

    @Command(
            aliases = {"disable", "remove"},
            desc = "Remove a mutation to the upcoming match." +
                    "You can use '?' as a wildcard or " +
                    "'*' to use all.",
            usage = "<mutation|?|*>",
            min = 1,
            max = 1
    )
    @CommandPermissions(PERMISSION_SET)
    public void disable(CommandContext args, CommandSender sender) throws CommandException {
        set(args, sender, false);
    }

    @Command(
            aliases = {"list"},
            desc = "List all the mutations options." +
                    "Use '-q' to see queued mutations.",
            usage = "[page]",
            flags = "q",
            min = 0,
            max = 1
    )
    @CommandPermissions(PERMISSION_LIST)
    public void list(final CommandContext args, CommandSender sender) throws CommandException {
        MutationMatchModule module = verify(sender);
        final boolean queued = args.hasFlag('q');
        final Collection<Mutation> active = queued ? mutationQueue.mutations() : module.getActiveMutations();
        new Paginator<Mutation>() {
            @Override
            protected BaseComponent title() {
                return new TranslatableComponent(queued ? "command.mutation.list.queued" : "command.mutation.list.current");
            }
            @Override
            protected BaseComponent entry(Mutation entry, int index) {
                return new Component(new BaseComponent[] {entry.getComponent(active.contains(entry) ? ChatColor.AQUA : ChatColor.GRAY)})
                        .extra(new Component(" (", ChatColor.WHITE).extra(new TranslatableComponent(entry.getDescription())).extra(")"));
            }
        }.display(sender, Sets.newHashSet(Mutation.values()), args.getInteger(0, 1));

    }

    public MutationMatchModule verify(CommandSender sender) throws CommandException {
        return CommandUtils.getMatchModule(MutationMatchModule.class, sender);
    }

    public void set(CommandContext args, final CommandSender sender, final boolean value) throws CommandException {
        final MutationMatchModule module = verify(sender);
        final Match match = module.getMatch();
        String action = args.getString(0);
        // Mutations that *will* be added or removed
        final Collection<Mutation> mutations = new HashSet<>();
        // Mutations that *are allowed* to be added or removed
        final Collection<Mutation> availableMutations = Sets.newHashSet(Mutation.values());

        final Collection<Mutation> queue = mutationQueue.mutations();
        if(value) availableMutations.removeAll(queue); else availableMutations.retainAll(queue);
        // Check if all mutations have been enabled/disabled
        if((queue.size() == Mutation.values().length && value) || (queue.isEmpty() && !value)) {
            throw newCommandException(sender, new TranslatableComponent(value ? "command.mutation.error.enabled" : "command.mutation.error.disabled"));
        }
        // Get which action the user wants to preform
        switch (action) {
            case "*": mutations.addAll(availableMutations); break;
            case "?": mutations.add(Iterables.get(availableMutations, RandomUtils.safeNextInt(match.getRandom(), availableMutations.size()))); break;
            default:
                Mutation query = StringUtils.bestFuzzyMatch(action, Sets.newHashSet(Mutation.values()), 0.9);
                if (query == null) {
                    throw newCommandException(sender, new TranslatableComponent("command.mutation.error.find", action));
                } else {
                    mutations.add(query);
                }
        }

        // Send the queued changes off to the api
        syncExecutor.callback(
            value ? mutationQueue.mergeAll(mutations)
                  : mutationQueue.removeAll(mutations),
            result -> {
                audiences.get(sender).sendMessage(new Component(new TranslatableComponent(
                    message(false, value, mutations.size() == 1),
                    new ListComponent(Collections2.transform(mutations, Mutation.toComponent(ChatColor.AQUA)))
                ), ChatColor.WHITE));
            }
        );
    }

    // TODO: force enabling mutations
    public String message(boolean now, boolean enable, boolean singular) {
        if(now) {
            if(enable) {
                return singular ? "command.mutation.enable.now.singular" : "command.mutation.enable.now.plural";
            } else {
                return singular ? "command.mutation.disable.now.singular" : "command.mutation.disable.now.plural";
            }
        } else {
            if(enable) {
                return singular ? "command.mutation.enable.later.singular" : "command.mutation.enable.later.plural";
            } else {
                return singular ? "command.mutation.disable.later.singular" : "command.mutation.disable.later.plural";
            }
        }
    }

}
