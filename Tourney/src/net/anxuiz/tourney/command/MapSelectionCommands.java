package net.anxuiz.tourney.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.sk89q.bukkit.util.BukkitWrappedCommandSender;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Console;
import com.sk89q.minecraft.util.commands.NestedCommand;
import net.anxuiz.tourney.ClassificationManager;
import net.anxuiz.tourney.MapClassification;
import net.anxuiz.tourney.TeamManager;
import net.anxuiz.tourney.Tourney;
import net.anxuiz.tourney.TourneyState;
import net.anxuiz.tourney.vote.VoteContext;
import org.apache.commons.collections.CollectionUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.api.docs.Entrant;
import tc.oc.commons.bukkit.commands.PrettyPaginatedResult;
import tc.oc.commons.core.commands.NestedCommands;
import tc.oc.pgm.commands.CommandUtils;
import tc.oc.pgm.map.MapInfo;
import tc.oc.pgm.map.PGMMap;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.inject.MatchScoped;

import static tc.oc.commons.core.exception.LambdaExceptionUtils.rethrowConsumer;

@MatchScoped
public class MapSelectionCommands implements NestedCommands {

    private final Tourney tourney;
    private final TeamManager teamManager;
    private final VoteContext voteContext;
    private final ClassificationManager classificationManager;
    private final Match match;

    @Inject MapSelectionCommands(Tourney tourney, TeamManager teamManager, VoteContext voteContext, ClassificationManager classificationManager, Match match) {
        this.tourney = tourney;
        this.teamManager = teamManager;
        this.voteContext = voteContext;
        this.classificationManager = classificationManager;
        this.match = match;
    }

    @Command(
            aliases = {"options", "remaining"},
            desc = "Displays remaining classifications or maps.",
            min = 0,
            max = 1,
            usage = "[page]"
    )
    @Console
    @CommandPermissions("tourney.map.veto")
    public void options(CommandContext args, CommandSender sender) throws CommandException {
        if (!tourney.getState().equals(TourneyState.ENABLED_MAP_SELECTION)) {
            throw new CommandException("There is no map selection vote in progress.");
        }

        voteContext.currentVote().ifPresent(rethrowConsumer(vote -> {
            if (vote.getSelectedClassification() != null) {
                new PrettyPaginatedResult<PGMMap>("Remaining Maps") {
                    @Override
                    public String format(PGMMap entry, int index) {
                        MapInfo info = entry.getInfo();
                        return org.bukkit.ChatColor.DARK_AQUA + "" + info.name + org.bukkit.ChatColor.GRAY + " " + info.version;
                    }
                }.display(new BukkitWrappedCommandSender(sender), vote.getRemainingMaps(), args.argsLength() > 0 ? args.getInteger(0) : 1);
            } else {
                final Set<PGMMap> remainingMaps = vote.getRemainingMaps();
                new PrettyPaginatedResult<MapClassification>("Remaining Classifications") {
                    @Override
                    public String format(MapClassification entry, int index) {
                        return org.bukkit.ChatColor.DARK_AQUA + "" + entry.name() + org.bukkit.ChatColor.GRAY + " (" + CollectionUtils.intersection(entry.maps(), remainingMaps).size() + " maps)";
                    }
                }.display(new BukkitWrappedCommandSender(sender), vote.getRemainingClassifications(), args.argsLength() > 0 ? args.getInteger(0) : 1);
            }
        }));
    }

    @Command(
            aliases = "veto",
            desc = "Vetoes a map or classification, indicating that it is the least desired of all available options.",
            min = 1,
            max = -1
    )
    @CommandPermissions("tourney.map.veto")
    public void veto(CommandContext args, CommandSender sender) throws CommandException {
        if (!(sender instanceof Player)) throw new CommandException("You must be a player to use this command.");

        if (!tourney.getState().equals(TourneyState.ENABLED_MAP_SELECTION)) {
            throw new CommandException("There is no map selection vote in progress.");
        }

        voteContext.currentVote().ifPresent(rethrowConsumer(vote -> {
            MatchPlayer player = match.getPlayer((Player) sender);
            Entrant participation = teamManager.teamToEntrant(player.getParty());

            if (participation == null) throw new CommandException("You are not participating in this veto.");
            if (!vote.getCurrentTurnRemainingTeams().contains(participation)) {
                throw new CommandException("You have already cast your veto for this turn.");
            }

            if (vote.getSelectedClassification() == null) {
                MapClassification classification = classificationManager.classificationFromSearch(args.getJoinedStrings(0));
                if (classification == null) throw new CommandException("No classification matched query.");
                if (!vote.getRemainingClassifications().contains(classification)) {
                    throw new CommandException("Specified classification is no longer available.");
                }

                try {
                    vote.registerVeto(participation, classification);
                } catch (IllegalArgumentException e) {
                    throw new CommandException(e.getMessage());
                }
            } else {
                PGMMap map = CommandUtils.getMap(args.getJoinedStrings(0));
                try {
                    vote.registerVeto(participation, map);
                } catch (IllegalArgumentException e) {
                    throw new CommandException(e.getMessage());
                }
            }
        }));
    }

    @Command(
            aliases = {"beginvote", "startvote"},
            desc = "Begins a map selection vote.",
            min = 0,
            max = 0,
            flags = "ct:"
    )
    @Console
    @CommandPermissions("tourney.map.beginvote")
    public void beginVote(CommandContext args, CommandSender sender) throws CommandException {
        if (!tourney.getState().equals(TourneyState.ENABLED_WAITING_FOR_READY)) {
            throw new CommandException("This match is not in a state that is eligible for map selection.");
        } else if (voteContext.voteInProgress()) {
            throw new CommandException("There is already a map selection vote in progress.");
        } else if (!args.hasFlag('c')) {
            throw new CommandException("Re-run command with -c to confirm. Map selection may not be ended pre-maturely.");
        }

        if (args.hasFlag('t')) {
            MapClassification classification = classificationManager.classificationFromSearch(args.getFlag('t'));
            if (classification == null) throw new CommandException("No classification matched query.");
            voteContext.startVote(Collections.singleton(classification));
        } else {
            voteContext.startVote(classificationManager.getClassifications());
        }
    }

    @Singleton
    public static class MapSelectionParentCommand implements NestedCommands {

        private final Tourney tourney;

        @Inject MapSelectionParentCommand(Tourney tourney) {
            this.tourney = tourney;
        }

        @Command(
                aliases = {"map", "mapselection"},
                desc = "Map selection-related commands"
        )
        @NestedCommand(value = MapSelectionCommands.class, executeBody = true)
        public void mapSelectionParentCommand(final CommandContext args, final CommandSender sender) throws CommandException {
            if (!Arrays.asList(TourneyState.ENABLED_MAP_SELECTION, TourneyState.ENABLED_WAITING_FOR_READY).contains(tourney.getState())) {
                throw new CommandException("This match is not in a state that is eligible for map selection.");
            }
        }
    }
}
