package tc.oc.pgm.match;

import java.util.List;
import javax.inject.Inject;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.CommandSender;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.chat.HeaderComponent;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Components;
import tc.oc.commons.core.formatting.PeriodFormats;
import tc.oc.commons.core.formatting.StringUtils;
import tc.oc.pgm.api.EngagementMatchModule;
import tc.oc.pgm.ffa.FreeForAllMatchModule;
import tc.oc.pgm.goals.Goal;
import tc.oc.pgm.goals.GoalComponent;
import tc.oc.pgm.goals.GoalMatchModule;
import tc.oc.pgm.score.ScoreMatchModule;
import tc.oc.pgm.teams.Team;
import tc.oc.pgm.teams.TeamMatchModule;

public class MatchFormatter {

    private final Audiences audiences;

    @Inject MatchFormatter(Audiences audiences) {
        this.audiences = audiences;
    }

    public void sendMatchInfo(CommandSender sender, Match match) {
        Audience audience = audiences.get(sender);

        final Component tip = new Component(ChatColor.WHITE).translate("command.match.matchInfo.title.tip");
        audience.sendMessage(new HeaderComponent(new Component(ChatColor.YELLOW)
                                                     .translate("command.match.matchInfo.title")
                                                     .extra(" #" + match.serialNumber() + " ")
                                                     .clickEvent(match.getUrl())
                                                     .hoverEvent(tip)));

        audience.sendMessage(new Component(
            new Component(ChatColor.DARK_PURPLE).extra(new TranslatableComponent("command.match.matchInfo.time")).extra(": "),
            new Component(ChatColor.GOLD).extra(PeriodFormats.formatColonsPrecise(match.runningTime()))
        ));

        final MatchPlayer player = match.getPlayer(sender);
        final TeamMatchModule tmm = match.getMatchModule(TeamMatchModule.class);
        final FreeForAllMatchModule ffamm = match.getMatchModule(FreeForAllMatchModule.class);
        final List<BaseComponent> teamCountParts = Lists.newArrayList();

        if(tmm != null) {
            for(Team team : tmm.getTeams()) {
                final Component msg = new Component(ChatColor.GRAY)
                    .extra(StringUtils.removeEnd(team.getName(), " Team"), team.getColor())
                    .extra(": ")
                    .extra(team.getPlayers().size(), ChatColor.WHITE);

                if(team.getMaxPlayers() != Integer.MAX_VALUE) {
                    msg.extra("/" + team.getMaxPlayers());
                }

                teamCountParts.add(msg);
            }
        } else if(ffamm != null) {
            teamCountParts.add(new Component(ChatColor.GRAY)
                                   .extra(new TranslatableComponent("command.match.matchInfo.players"), ChatColor.YELLOW)
                                   .extra(": ")
                                   .extra(match.getParticipatingPlayers().size(), ChatColor.WHITE)
                                   .extra("/" + ffamm.getMaxPlayers()));
        }

        teamCountParts.add(new Component(ChatColor.GRAY)
                           .extra(new TranslatableComponent("command.match.matchInfo.observers"), ChatColor.AQUA)
                           .extra(": ")
                           .extra(match.getObservingPlayers().size(), ChatColor.WHITE));

        audience.sendMessage(Components.join(new Component(" | ", ChatColor.DARK_GRAY), teamCountParts));

        match.module(GoalMatchModule.class).ifPresent(gmm -> {
            if(tmm != null && gmm.getGoalsByCompetitor().size() > 0) {
                final ListMultimap<Team, BaseComponent> teamGoalTexts = ArrayListMultimap.create();
                for(Team team : tmm.getTeams()) {
                    for(Goal goal : gmm.getGoals(team)) {
                        if(goal.isVisible()) {
                            teamGoalTexts.put(team, GoalComponent.forCompetitor(goal, team, true));
                        }
                    }
                }

                if(!teamGoalTexts.isEmpty()) {
                    audience.sendMessage(new Component(ChatColor.DARK_PURPLE)
                                             .translate("command.match.matchInfo.goals")
                                             .extra(":"));

                    teamGoalTexts.asMap().forEach((team, goalTexts) -> {
                        audience.sendMessage(new Component(ChatColor.GRAY)
                                                 .extra("  ")
                                                 .extra(team.getComponentName())
                                                 .extra(": ")
                                                 .extra(Components.join(new Component("  "), goalTexts)));
                    });
                }
            } else {
                // FIXME: better display for FFA
                match.module(ScoreMatchModule.class).ifPresent(smm -> {
                    audience.sendMessage(smm.getStatusMessage());
                });
            }
        });

        audience.sendMessage(new Component(ChatColor.DARK_PURPLE)
                                 .translate("misc.link")
                                 .extra(": ")
                                 .link(match.getUrl())
                                 .hoverEvent(tip));

        if(player != null) match.module(EngagementMatchModule.class).ifPresent(emm -> {
            emm.sendPreMatchFeedback(player);
        });
    }
}
