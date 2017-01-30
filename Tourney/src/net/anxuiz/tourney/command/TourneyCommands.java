package net.anxuiz.tourney.command;

import java.util.Arrays;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;
import com.sk89q.minecraft.util.commands.ChatColor;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Console;
import com.sk89q.minecraft.util.commands.NestedCommand;
import net.anxuiz.tourney.ReadyManager;
import net.anxuiz.tourney.Tourney;
import net.anxuiz.tourney.TourneyPermissions;
import net.anxuiz.tourney.TourneyState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.commands.NestedCommands;
import tc.oc.pgm.commands.CommandUtils;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.Party;

import static tc.oc.commons.bukkit.commands.CommandUtils.assertPermission;

@Singleton
public class TourneyCommands implements NestedCommands {

    @Command(
            aliases = {"ready"},
            desc = "Indicates that the executing team is ready.",
            min = 0,
            max = 0
    )
    @CommandPermissions(TourneyPermissions.READY)
    public static void ready(final CommandContext args, final CommandSender sender) throws CommandException {
        Tourney plugin = Tourney.get();
        if (!(plugin.getState().equals(TourneyState.ENABLED_WAITING_FOR_READY))) {
            throw new CommandException("This match is not in a ready-able state.");
        }

        final Match match = CommandUtils.getMatch(sender);
        final Party party = match.player(sender)
                                 .map(MatchPlayer::getParty)
                                 .orElse(match.getDefaultParty());

        if(party.isObservingType()) {
            assertPermission(sender, TourneyPermissions.READY_OBSERVER);
        }

        ReadyManager readyManager = Preconditions.checkNotNull(plugin.getMatchManager(), "Match manager").getReadyManager();
        int minPlayers = plugin.getTournament().min_players_per_match();
        int presentPlayers = party.getPlayers().size();
        if (party.isParticipatingType() && minPlayers > presentPlayers) {
            throw new CommandException("You need at least " + (minPlayers - presentPlayers) + " more players present to ready your team.");
        } else {
            if (!readyManager.isReady(party)) {
                readyManager.markReady(party);
            } else {
                throw new CommandException("Your team is already ready.");
            }
        }
    }

    @Command(
            aliases = {"unready"},
            desc = "Indicates that the executing team is no longer ready.",
            min = 0,
            max = 0
    )
    @CommandPermissions(TourneyPermissions.READY)
    public static void unready(final CommandContext args, final CommandSender sender) throws CommandException {
        if (!(sender instanceof Player)) throw new CommandException("You must be a player to use this command.");
        Tourney plugin = Tourney.get();
        if (!(Arrays.asList(TourneyState.ENABLED_STARTING, TourneyState.ENABLED_WAITING_FOR_READY).contains(plugin.getState()))) {
            throw new CommandException("This match is not in an un-ready-able state.");
        }

        MatchPlayer player = CommandUtils.senderToMatchPlayer(sender);

        Party party = player.getParty();
        if(party.isObservingType()) {
            assertPermission(sender, TourneyPermissions.READY_OBSERVER);
        }

        ReadyManager readyManager = Preconditions.checkNotNull(plugin.getMatchManager(), "Match manager").getReadyManager();
        if (readyManager.isReady(party)) {
            readyManager.markNotReady(party);
        } else {
            throw new CommandException("Your team is not ready.");
        }
    }

    @Command(
            aliases = {"invalidate", "nosave", "norecord"},
            desc = "Indicates that the current match should not be saved to the database.",
            min = 0,
            max = 0,
            flags = "c"
    )
    @Console
    @CommandPermissions("tourney.invalidate")
    public static void invalidate(final CommandContext args, final CommandSender sender) throws CommandException {
        Tourney plugin = Tourney.get();
        if (!Arrays.asList(TourneyState.ENABLED_RUNNING, TourneyState.ENABLED_FINISHED).contains(plugin.getState())) {
            throw new CommandException("This match may not be invalidated at this time.");
        }

        if (plugin.isRecordQueued()) {
            if (args.hasFlag('c')) {
                plugin.setRecordQueued(false);
                sender.sendMessage(ChatColor.YELLOW + "Match successfully invalidated.");
            } else {
                throw new CommandException("Match is eligible for invalidation. Re-run command with -c to confirm. Invalidation may not be reversed.");
            }
        } else {
            throw new CommandException("This match is not queued to be recorded.");
        }
    }

    @Singleton
    public static class TourneyParentCommand implements Commands {
        @Command(
                aliases = {"tourney", "tournament", "tm"},
                desc = "Tournament-related commands"
        )
        @NestedCommand({TourneyCommands.class, TeamCommands.class, MapSelectionCommands.MapSelectionParentCommand.class})
        public void tourneyParentCommand(final CommandContext args, final CommandSender sender) {
            // never executed
        }
    }
}
