package tc.oc.pgm.commands;

import com.google.common.collect.Sets;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.NestedCommand;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.commons.bukkit.tokens.TokenUtil;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.formatting.StringUtils;
import tc.oc.commons.core.restart.RestartManager;
import tc.oc.parse.ParseException;
import tc.oc.parse.primitive.BooleanParser;
import tc.oc.pgm.Config;
import tc.oc.pgm.PGM;
import tc.oc.pgm.map.PGMMap;
import tc.oc.pgm.mutation.Mutation;
import tc.oc.pgm.mutation.MutationMatchModule;
import tc.oc.pgm.mutation.command.MutationCommands;
import tc.oc.pgm.polls.Poll;
import tc.oc.pgm.polls.PollCustom;
import tc.oc.pgm.polls.PollEndReason;
import tc.oc.pgm.polls.PollKick;
import tc.oc.pgm.polls.PollManager;
import tc.oc.pgm.polls.PollMutation;
import tc.oc.pgm.polls.PollNextMap;

import javax.inject.Inject;

import java.util.List;

import static tc.oc.commons.bukkit.commands.CommandUtils.newCommandException;

public class PollCommands implements Commands {

    private static final String VOTE_FOR = ChatColor.GREEN + "in favor of";
    private static final String VOTE_AGAINST = ChatColor.RED + "against";

    @Inject
    private static RestartManager restartManager;
    @Inject
    private static BooleanParser booleanParser;

    @Command(
        aliases = {"poll"},
        desc = "Poll commands",
        min = 1,
        max = -1
    )
    @NestedCommand({PollSubCommands.class})
    public static void pollCommand() {
    }

    @Command(
        aliases = {"vote"},
        desc = "Vote in a running poll.",
        usage = "[yes|no]",
        min = 1,
        max = 1
    )
    @CommandPermissions("poll.vote")
    public static void vote(CommandContext args, CommandSender sender) throws CommandException {
        Player voter = tc.oc.commons.bukkit.commands.CommandUtils.senderToPlayer(sender);
        Poll currentPoll = PGM.getPollManager().getPoll();
        if(currentPoll != null) {
            boolean vote;
            try {
                vote = booleanParser.parse(args.getString(0));
            } catch (ParseException e) {
                throw new CommandException("Please vote yes or no!");
            }
            currentPoll.vote(vote, voter.getName());
            sender.sendMessage(ChatColor.AQUA + "You voted " + (vote ? VOTE_FOR : VOTE_AGAINST) + ChatColor.AQUA + " the current poll.");
        } else {
            throw new CommandException("There is currently no poll running.");
        }
    }

    @Command(
       aliases = {"veto"},
       desc = "Veto the current poll.",
       min = 0,
       max = 0
    )
    @CommandPermissions("poll.veto")
    public static void veto(CommandContext args, CommandSender sender) throws CommandException {
        PollManager pollManager = PGM.getPollManager();
        if(pollManager.isPollRunning()) {
            pollManager.endPoll(PollEndReason.Cancelled);
            Bukkit.getServer().broadcastMessage(ChatColor.RED + "Poll vetoed by " + sender.getName());
        } else {
            throw new CommandException("There is currently no poll running.");
        }
    }

    public static Poll getCurrentPoll() throws CommandException {
        Poll poll = PGM.getPollManager().getPoll();
        if(poll == null) {
            throw new CommandException("There is currently no poll running.");
        }
        return poll;
    }

    public static class PollSubCommands {
        @Command(
            aliases = {"kick"},
            desc = "Start a poll to kick another player.",
            usage = "[player]",
            min = 1,
            max = 1
        )
        @CommandPermissions("poll.kick")
        public static void pollKick(CommandContext args, CommandSender sender) throws CommandException {
            Player initiator = tc.oc.commons.bukkit.commands.CommandUtils.senderToPlayer(sender);
            Player player = tc.oc.commons.bukkit.commands.CommandUtils.findOnlinePlayer(args, sender, 0);

            if(player.hasPermission("pgm.poll.kick.exempt") && !initiator.hasPermission("pgm.poll.kick.override")) {
                throw new CommandException(player.getName() + " may not be poll kicked.");
            } else {
                startPoll(new PollKick(PGM.getPollManager(), Bukkit.getServer(), initiator.getName(), player.getName()));
            }
        }

        @Command(
            aliases = {"next"},
            desc = "Start a poll to change the next map.",
            usage = "[map name]",
            min = 1,
            max = -1
        )
        @CommandPermissions("poll.next")
        public static List<String> pollNext(CommandContext args, CommandSender sender) throws CommandException {
            final String mapName = args.argsLength() > 0 ? args.getJoinedStrings(0) : "";
            if(args.getSuggestionContext() != null) {
                return CommandUtils.completeMapName(mapName);
            }

            if (!Config.Poll.enabled()) {
                throw newCommandException(sender, new TranslatableComponent("poll.disabled"));
            }

            if (restartManager.isRestartRequested()) {
                throw newCommandException(sender, new TranslatableComponent("poll.map.restarting"));
            }
            if (PGM.getMatchManager().hasMapSet()) {
                throw newCommandException(sender, new TranslatableComponent("poll.map.alreadyset"));
            }
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (TokenUtil.getUser(player).maptokens() < 1) {
                    throw newCommandException(sender, new TranslatableComponent("tokens.map.fail"));
                }
            }

            Player initiator = tc.oc.commons.bukkit.commands.CommandUtils.senderToPlayer(sender);

            PGMMap nextMap = CommandUtils.getMap(args.getJoinedStrings(0), sender);

            if (!PGM.getPollableMaps().isAllowed(nextMap) && !sender.hasPermission("poll.next.override")) {
                throw newCommandException(sender, new TranslatableComponent("poll.map.notallowed"));
            }

            if (PGM.get().getServer().getOnlinePlayers().size() * 4 / 5 > nextMap.getDocument().max_players() && !sender.hasPermission("poll.next.override")) {
                throw newCommandException(sender, new TranslatableComponent("poll.map.toomanyplayers"));
            }

            startPoll(new PollNextMap(PGM.getPollManager(), Bukkit.getServer(), sender,  initiator.getName(), PGM.getMatchManager(), nextMap));
            return null;
        }

        @Command(
                aliases = {"mutation", "mt"},
                desc = "Start a poll to set a mutation",
                usage = "[mutation name]",
                min = 1,
                max = -1
        )
        @CommandPermissions("poll.mutation")
        public static void pollMutation(CommandContext args, CommandSender sender) throws CommandException {

            if (!Config.Poll.enabled()) {
                throw newCommandException(sender, new TranslatableComponent("poll.disabled"));
            }

            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (TokenUtil.getUser(player).mutationtokens() < 1) {
                    throw newCommandException(sender, new TranslatableComponent("tokens.mutation.fail"));
                }
            }

            String mutationString = args.getString(0);
            MutationMatchModule module = PGM.getMatchManager().getCurrentMatch(sender).getMatchModule(MutationMatchModule.class);

            Mutation mutation = StringUtils.bestFuzzyMatch(mutationString, Sets.newHashSet(Mutation.values()), 0.9);
            if(mutation == null) {
                throw newCommandException(sender, new TranslatableComponent("command.mutation.error.find", mutationString));
            } else if(MutationCommands.getInstance().getMutationQueue().mutations().contains(mutation)) {
                throw newCommandException(sender, new TranslatableComponent(true ? "command.mutation.error.enabled" : "command.mutation.error.disabled", mutation.getComponent(net.md_5.bungee.api.ChatColor.RED)));
            } else if (!mutation.isPollable() && !sender.hasPermission("poll.mutation.override")) {
                throw newCommandException(sender, new TranslatableComponent("command.mutation.error.illegal", mutationString));
            }

            startPoll(new PollMutation(PGM.getPollManager(), Bukkit.getServer(), sender, mutation, module));
        }

        @Command(
                aliases = {"custom"},
                desc = "Start a poll with the supplied text",
                usage = "[text...]",
                min = 1,
                max = -1
        )
        @CommandPermissions("poll.custom")
        public static void pollCustom(CommandContext args, CommandSender sender) throws CommandException {
            String text = args.getJoinedStrings(0);
            Player initiator = tc.oc.commons.bukkit.commands.CommandUtils.senderToPlayer(sender);

            startPoll(new PollCustom(PGM.getPollManager(), Bukkit.getServer(), initiator.getName(), text));
        }

        public static void startPoll(Poll poll) throws CommandException {
            PollManager pollManager = PGM.getPollManager();
            if(pollManager.isPollRunning()) {
                throw new CommandException("Another poll is already running.");
            }
            pollManager.startPoll(poll);
            Bukkit.getServer().broadcastMessage(Poll.boldAqua + poll.getInitiator() + Poll.normalize + " has started a poll " + poll.getDescriptionMessage());
            Bukkit.broadcast(Poll.tutorialMessage());
        }
    }
}
