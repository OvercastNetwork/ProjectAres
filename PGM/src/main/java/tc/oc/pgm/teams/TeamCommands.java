package tc.oc.pgm.teams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.NestedCommand;
import com.sk89q.minecraft.util.commands.SuggestException;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import tc.oc.commons.bukkit.localization.Translations;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.commands.NestedCommands;
import tc.oc.commons.core.commands.TranslatableCommandException;
import tc.oc.pgm.PGMTranslations;
import tc.oc.pgm.commands.CommandUtils;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.inject.MatchScoped;

@MatchScoped
public class TeamCommands implements NestedCommands {

    @Singleton
    public static class Parent implements Commands {
        @Command(
                aliases = {"team"},
                desc = "Commands for working with teams",
                min = 1,
                max = -1
        )
        @NestedCommand({TeamCommands.class})
        public void team() {}
    }

    private final TeamCommandUtils utils;
    private final Set<Team> teams;

    @Inject TeamCommands(TeamCommandUtils utils, Set<Team> teams) {
        this.utils = utils;
        this.teams = teams;
    }

    @Command(
            aliases = {"myteam", "mt"},
            desc = "Shows you what team you are on",
            min = 0,
            max = 0
    )
    @CommandPermissions("pgm.myteam")
    public void myteam(CommandContext args, CommandSender sender) throws CommandException {
        MatchPlayer player = CommandUtils.senderToMatchPlayer(sender);
        if(player.getParty() instanceof Team) {
            sender.sendMessage(ChatColor.GRAY + PGMTranslations.t("command.gameplay.myteam.message", player, player.getParty().getColoredName() + ChatColor.GRAY));
        } else {
            throw new CommandException(PGMTranslations.get().t("command.gameplay.myteam.notOnTeam", sender));
        }
    }

    @Command(
            aliases = {"force"},
            desc = "Force a player onto a team",
            usage = "<player> [team]",
            min = 1,
            max = 2
    )
    @CommandPermissions("pgm.team.force")
    public void force(CommandContext args, CommandSender sender) throws CommandException, SuggestException {
        MatchPlayer player = CommandUtils.findSingleMatchPlayer(args, sender, 0);

        if(args.argsLength() >= 2) {
            String name = args.getString(1);
            if(name.trim().toLowerCase().startsWith("obs")) {
                player.getMatch().setPlayerParty(player, player.getMatch().getDefaultParty());
            } else {
                Team team = utils.teamArgument(args, 1);
                utils.module().forceJoin(player, team);
            }
        } else {
            utils.module().forceJoin(player, null);
        }
    }

    @Command(
            aliases = {"shuffle"},
            desc = "Shuffle the teams",
            min = 0,
            max = 0
    )
    @CommandPermissions("pgm.team.shuffle")
    public void shuffle(CommandContext args, CommandSender sender) throws CommandException {
        TeamMatchModule tmm = utils.module();
        Match match = tmm.getMatch();

        if(match.isRunning()) {
            throw new CommandException(Translations.get().t("command.team.shuffle.matchRunning", sender));
        } else {
            List<Team> teams = new ArrayList<>(this.teams);
            List<MatchPlayer> participating = new ArrayList<>(match.getParticipatingPlayers());
            Collections.shuffle(participating);
            for(int i = 0; i < participating.size(); i++) {
                tmm.forceJoin(participating.get(i), teams.get((i * teams.size()) / participating.size()));
            }
            match.sendMessage(new TranslatableComponent("command.team.shuffle.success"));
        }
    }

    @Command(
            aliases = {"alias"},
            desc = "Rename a team",
            usage = "<old name> <new name>",
            min = 2,
            max = -1
    )
    @CommandPermissions("pgm.team.alias")
    public void alias(CommandContext args, CommandSender sender) throws CommandException, SuggestException {
        TeamMatchModule tmm = utils.module();
        Match match = tmm.getMatch();
        Team team = utils.teamArgument(args, 0);

        String newName = args.getJoinedStrings(1);

        if(newName.length() > 32) {
            throw new CommandException("Team name cannot be longer than 32 characters");
        }

        if(teams.stream().anyMatch(t -> t.getName().equalsIgnoreCase(newName))) {
            throw new TranslatableCommandException("command.team.alias.nameAlreadyUsed", newName);
        }

        String oldName = team.getColoredName();
        team.setName(newName);

        match.sendMessage(oldName + ChatColor.GRAY + " renamed to " + team.getColoredName());
    }

    @Command(
            aliases = {"max", "size"},
            desc = "Change the maximum size of a team. If max-overfill is not specified, it will be the same as max-players.",
            usage = "<team> (default | <max-players> [max-overfill])",
            min = 2,
            max = 3
    )
    @CommandPermissions("pgm.team.size")
    public void max(CommandContext args, CommandSender sender) throws CommandException, SuggestException {

        int maxPlayers = args.getInteger(1);
        if(maxPlayers < 0) throw new CommandException("max-players cannot be less than 0");

        Integer maxOverfill = null;
        if(args.argsLength() == 3) {
            maxOverfill = args.getInteger(2);
            if (maxOverfill < maxPlayers) throw new CommandException("max-overfill cannot be less than max-players");
        }

        List<Team> teams = new ArrayList<>();

        if (args.getString(0).equals("*")) {
            List<String> teamNames = utils.teamNames();
            for (String teamName: teamNames) {
                if (!teamName.toLowerCase().contains("obs")) {
                    teams.add(utils.team(teamName));
                }
            }
        } else {
            teams.add(utils.teamArgument(args, 0));
        }

        for (Team team: teams) {
            if("default".equals(args.getString(1))) {
                team.resetMaxSize();
            } else {
                team.setMaxSize(maxPlayers, maxOverfill != null ? maxOverfill : maxPlayers);
            }
        }

        if (teams.size() == 1) {
            sender.sendMessage(teams.get(0).getColoredName() +
                    ChatColor.WHITE + " now has max size " + ChatColor.AQUA + teams.get(0).getMaxPlayers() +
                    ChatColor.WHITE + " and max overfill " + ChatColor.AQUA + teams.get(0).getMaxOverfill());
        } else if (teams.size() > 1) {
            sender.sendMessage("All teams" +
                    ChatColor.WHITE + " now have max size " + ChatColor.AQUA + teams.get(0).getMaxPlayers() +
                    ChatColor.WHITE + " and max overfill " + ChatColor.AQUA + teams.get(0).getMaxOverfill());
        }
    }

    @Command(
            aliases = {"min"},
            desc = "Change the minimum size of a team.",
            usage = "<team> (default | <min-players>)",
            min = 2,
            max = 2
    )
    @CommandPermissions("pgm.team.size")
    public void min(CommandContext args, CommandSender sender) throws CommandException, SuggestException {
        Team team = utils.teamArgument(args, 0);

        if("default".equals(args.getString(1))) {
            team.resetMinSize();
        } else {
            int minPlayers = args.getInteger(1);
            if(minPlayers < 0) throw new CommandException("min-players cannot be less than 0");
            team.setMinSize(minPlayers);
        }

        sender.sendMessage(team.getColoredName() +
                ChatColor.WHITE + " now has min size " + ChatColor.AQUA + team.getMinPlayers());
    }
}
