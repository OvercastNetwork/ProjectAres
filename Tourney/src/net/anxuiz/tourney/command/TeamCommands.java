package net.anxuiz.tourney.command;

import java.util.Arrays;
import javax.inject.Inject;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Console;
import com.sk89q.minecraft.util.commands.SuggestException;
import net.anxuiz.tourney.TeamManager;
import net.anxuiz.tourney.Tourney;
import net.anxuiz.tourney.TourneyState;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.docs.Entrant;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.Tournament;
import tc.oc.api.docs.team;
import tc.oc.api.exceptions.NotFound;
import tc.oc.api.tourney.TeamUtils;
import tc.oc.api.tourney.TournamentService;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.chat.Paginator;
import tc.oc.commons.bukkit.chat.PlayerComponent;
import tc.oc.commons.bukkit.chat.WarningComponent;
import tc.oc.commons.bukkit.nick.IdentityProvider;
import tc.oc.minecraft.scheduler.MainThreadExecutor;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.commands.CommandFutureCallback;
import tc.oc.commons.core.commands.NestedCommands;
import tc.oc.commons.core.commands.TranslatableCommandException;
import tc.oc.commons.core.formatting.StringUtils;
import tc.oc.pgm.match.inject.MatchScoped;
import tc.oc.pgm.teams.Team;
import tc.oc.pgm.teams.TeamCommandUtils;

@MatchScoped
public class TeamCommands implements NestedCommands {

    private final BukkitUserStore userStore;
    private final Tournament tournament;
    private final BaseComponent tournamentName;
    private final TournamentService tournamentService;
    private final Audiences audiences;
    private final Tourney tourney;
    private final TeamManager teamManager;
    private final TeamCommandUtils teamCommandUtils;
    private final IdentityProvider identityProvider;
    private final MainThreadExecutor mainThread;

    @Inject TeamCommands(Tournament tournament,
                         BukkitUserStore userStore,
                         TournamentService tournamentService,
                         Audiences audiences,
                         Tourney tourney,
                         TeamManager teamManager,
                         TeamCommandUtils teamCommandUtils,
                         IdentityProvider identityProvider,
                         MainThreadExecutor mainThread) {

        this.tournament = tournament;
        this.tournamentName = new Component(tournament.name(), ChatColor.GREEN);
        this.userStore = userStore;
        this.tournamentService = tournamentService;
        this.audiences = audiences;
        this.tourney = tourney;
        this.teamManager = teamManager;
        this.teamCommandUtils = teamCommandUtils;
        this.identityProvider = identityProvider;
        this.mainThread = mainThread;
    }

    private BaseComponent teamName(team.Id team) {
        return new Component(team.name(), ChatColor.YELLOW);
    }

    private BaseComponent teamName(Entrant entrant) {
        return teamName(entrant.team());
    }

    private team.Id findTeam(String name) throws CommandException {
        return StringUtils.fuzzyMatch(TeamUtils.normalizeName(name),
                                      Maps.uniqueIndex(tournament.accepted_teams(), team.Id::name_normalized),
                                      0.9)
                          .orElseThrow(() -> new TranslatableCommandException("tourney.team.notFound", tournamentName, name));
    }

    private team.Id teamArgument(CommandContext args, int index) throws SuggestException, CommandException {
        return findTeam(args.joinedStrings(index, tournament.acceptedTeamNames()));
    }

    @Command(
            aliases = {"roster", "players"},
            usage = "[team]",
            desc = "Lists the players on a specified team.",
            flags = "p:",
            min = 0,
            max = -1
    )
    @Console
    @CommandPermissions("tourney.roster")
    public void roster(final CommandContext args, final CommandSender sender) throws CommandException, SuggestException {
        final int page = args.getFlagInteger('p', 1);
        final ListenableFuture<Entrant> futureEntrant;
        if(args.argsLength() > 0) {
            futureEntrant = tournamentService.entrant(tournament, teamArgument(args, 0));
        } else if(sender instanceof Player) {
            futureEntrant = tournamentService.entrantByMember(tournament, userStore.playerId((Player) sender));
        } else {
            throw new TranslatableCommandException("tourney.team.notSpecified");
        }

        final Audience audience = audiences.get(sender);

        mainThread.callback(
            futureEntrant,
            CommandFutureCallback.<Entrant>onSuccess(sender, args, entrant -> {
                new Paginator<PlayerId>()
                    .title(new TranslatableComponent("tourney.team.roster.title",
                                                     tournamentName,
                                                     teamName(entrant)))
                    .entries((playerId, index) ->
                                 new Component(new PlayerComponent(identityProvider.createIdentity(playerId)))
                                     .bold(index == 0)) // Team leader is always first
                    .display(audience, entrant.members(), page);
            }).onFailure(NotFound.class, notFound -> {
                audience.sendMessage(new WarningComponent("tourney.team.notOnAnyTeam"));
            })
        );
    }

    @Command(
            aliases = {"teams", "listteams"},
            desc = "Lists the teams registered for competition on this server.",
            min = 0,
            max = 1,
            usage = "[page]"
    )
    @Console
    @CommandPermissions("tourney.listteams")
    public void listTeams(final CommandContext args, final CommandSender sender) throws CommandException {
        new Paginator<team.Id>()
            .title(new TranslatableComponent("tourney.teams.title", tournamentName))
            .entries((team, index) -> teamName(team))
            .display(audiences.get(sender),
                     tournament.accepted_teams(),
                     args.getInteger(0, 1));
    }

    @Command(
            aliases = {"register", "whitelist", "add"},
            desc = "Registers a team for competition.",
            help = "Registers the specified team, entering them into the competition.",
            max = -1,
            min = 1,
            flags = "t:",
            usage = "[-t <map team>] <tournament team>"
    )
    @Console
    @CommandPermissions("tourney.addteam")
    public void register(final CommandContext args, final CommandSender sender) throws CommandException, SuggestException {
        if(!Arrays.asList(TourneyState.DISABLED, TourneyState.ENABLED_WAITING_FOR_TEAMS).contains(tourney.getState())) {
            throw new TranslatableCommandException("tourney.team.cannotRegister");
        }

        final Audience audience = audiences.get(sender);
        final Team matchTeam = teamCommandUtils.teamFlag(args, 't').orElse(null);
        final team.Id team = teamArgument(args, 0);

        mainThread.callback(
            tournamentService.entrant(tournament, team),
            CommandFutureCallback.onSuccess(sender, args, entrant -> {
                Team assignedTeam = teamManager.entrantToTeam(entrant);
                if(assignedTeam != null) {
                    throw new TranslatableCommandException(
                        "tourney.team.alreadyRegistered",
                        tournamentName,
                        teamName(entrant),
                        assignedTeam.getComponentName()
                    );
                }

                assignedTeam = teamManager.assignEntrant(entrant, matchTeam);
                if(assignedTeam == null) {
                    throw new TranslatableCommandException("tourney.team.cannotRegister");
                }

                audience.sendMessage(new TranslatableComponent(
                    "tourney.team.registered",
                    tournamentName,
                    new Component(entrant.team().name(), ChatColor.YELLOW),
                    assignedTeam.getDefinition().getComponentName()
                ));
            })
        );
    }

    @Command(
            aliases = {"unregister", "remove"},
            desc = "Un-registers a team from competition.",
            help = "Un-registers the specified team, ejecting them from the competition.",
            max = -1,
            min = 1,
            usage = "<team>"
    )
    @Console
    @CommandPermissions("tourney.removeteam")
    public void unregister(final CommandContext args, final CommandSender sender) throws CommandException, SuggestException {
        if(!Arrays.asList(TourneyState.DISABLED, TourneyState.ENABLED_WAITING_FOR_TEAMS).contains(tourney.getState())) {
            throw new TranslatableCommandException("tourney.team.cannotUnregister");
        }

        final Audience audience = audiences.get(sender);
        final String teamName = args.joinedStrings(0, Iterables.transform(teamManager.mappedTeams().values(),
                                                                             entrant -> entrant.team().name()));
        final Entrant entrant = teamManager.getEntrant(teamName);
        if(entrant == null) {
            throw new TranslatableCommandException("tourney.team.notFound", tournamentName, teamName);
        }

        if(teamManager.unmap(entrant)) {
            audience.sendMessage(new TranslatableComponent("tourney.team.unregistered", tournamentName, teamName(entrant)));
        } else {
            throw new TranslatableCommandException("tourney.team.cannotUnregister");
        }
    }
}
