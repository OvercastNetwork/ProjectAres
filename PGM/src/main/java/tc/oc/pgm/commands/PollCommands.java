package tc.oc.pgm.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tc.oc.pgm.PGM;
import tc.oc.pgm.map.PGMMap;
import tc.oc.pgm.polls.*;

import com.sk89q.minecraft.util.commands.*;

public class PollCommands {
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
            if(args.getString(0).equalsIgnoreCase("yes")) {
                currentPoll.voteFor(voter.getName());
                sender.sendMessage(ChatColor.GREEN + "You have voted for the current poll.");
            } else if (args.getString(0).equalsIgnoreCase("no")) {
                currentPoll.voteAgainst(voter.getName());
                sender.sendMessage(ChatColor.RED + "You have voted against the current poll.");
            } else {
                throw new CommandException("Accepted values: yes|no");
            }
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
        public static void pollNext(CommandContext args, CommandSender sender) throws CommandException {
            Player initiator = tc.oc.commons.bukkit.commands.CommandUtils.senderToPlayer(sender);
            PGMMap nextMap = CommandUtils.getMap(args.getJoinedStrings(0), sender);
            startPoll(new PollNextMap(PGM.getPollManager(), Bukkit.getServer(), initiator.getName(), PGM.getMatchManager(), nextMap));
        }

        private static void startPoll(Poll poll) throws CommandException {
            PollManager pollManager = PGM.getPollManager();
            if(pollManager.isPollRunning()) {
                throw new CommandException("Another poll is already running.");
            }
            pollManager.startPoll(poll);
            Bukkit.getServer().broadcastMessage(ChatColor.RED + poll.getInitiator() + ChatColor.YELLOW + " has started a poll to " + poll.getActionString(ChatColor.YELLOW));
        }
    }
}
