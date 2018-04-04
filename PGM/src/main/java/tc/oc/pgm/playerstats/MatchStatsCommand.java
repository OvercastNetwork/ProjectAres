package tc.oc.pgm.playerstats;

import com.google.common.collect.Lists;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import java.text.DecimalFormat;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.CommandSender;
import tc.oc.commons.bukkit.chat.HeaderComponent;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.commons.bukkit.commands.UserFinder;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Components;
import tc.oc.commons.core.commands.CommandFutureCallback;
import tc.oc.commons.core.concurrent.Flexecutor;
import tc.oc.minecraft.scheduler.Sync;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.inject.MatchScoped;

import static tc.oc.pgm.commands.CommandUtils.senderToMatchPlayer;

import javax.inject.Inject;

@MatchScoped
public class MatchStatsCommand {
    private final UserFinder userFinder;
    private final Flexecutor flexecutor;

    private static final DecimalFormat FORMAT = new DecimalFormat("0.00");

    @Inject MatchStatsCommand(UserFinder userFinder, @Sync Flexecutor flexecutor) {
        this.userFinder = userFinder;
        this.flexecutor = flexecutor;
    }

    @Command(aliases = {"matchstats", "mstats"},
             desc = "Displays your current stats for the match",
             usage = "[target]",
             max = 1
    )
    @CommandPermissions("pgm.playerstats.matchstats")
    public void matchStats(CommandContext args, CommandSender sender) throws CommandException {
        if(args.argsLength() == 0) {
            displayStats(senderToMatchPlayer(sender));
        } else {
            flexecutor.callback(
                userFinder.findLocalPlayerOrSender(sender, args, 0),
                CommandFutureCallback.onSuccess(sender, user -> displayStats(senderToMatchPlayer(user.player())))
            );
        }
    }

    private BaseComponent parseStats(MatchPlayer player) {
        StatsUserFacet facet = player.getUserContext().facet(StatsUserFacet.class);
        BaseComponent matchKills = new Component(ChatColor.GREEN).translate("command.matchstats.kills", facet.matchKills());
        BaseComponent matchDeaths = new Component(ChatColor.RED).translate("command.matchstats.deaths", facet.deaths());
        BaseComponent matchRatio = new Component(ChatColor.AQUA).translate("command.matchstats.kdr", FORMAT.format((double) facet.matchKills() / Math.max(facet.deaths(), 1)));
        return Components.join(Components.newline(), Lists.newArrayList(matchKills, matchDeaths, matchRatio));
    }

    private void displayStats(MatchPlayer player) {
        player.sendMessage(new HeaderComponent(new TranslatableComponent("command.matchstats.header", player.getStyledName(NameStyle.VERBOSE))));
        player.sendMessage(parseStats(player));
    }
}
