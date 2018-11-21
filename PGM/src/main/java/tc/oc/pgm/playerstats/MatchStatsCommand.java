package tc.oc.pgm.playerstats;

import com.google.common.collect.Lists;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import java.text.DecimalFormat;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.CommandSender;
import tc.oc.api.bukkit.users.OnlinePlayers;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.chat.HeaderComponent;
import tc.oc.commons.bukkit.chat.PlayerComponent;
import tc.oc.commons.bukkit.commands.UserFinder;
import tc.oc.commons.bukkit.nick.IdentityProvider;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Components;
import tc.oc.commons.core.commands.CommandFutureCallback;
import tc.oc.commons.core.concurrent.Flexecutor;
import tc.oc.commons.core.formatting.StringUtils;
import tc.oc.minecraft.scheduler.Sync;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.inject.MatchScoped;

import static tc.oc.pgm.commands.CommandUtils.senderToMatchPlayer;

import javax.inject.Inject;

@MatchScoped
public class MatchStatsCommand {
    private final UserFinder userFinder;
    private final Flexecutor flexecutor;
    private final OnlinePlayers onlinePlayers;
    private final IdentityProvider identityProvider;
    private final Audiences audiences;

    private static final DecimalFormat FORMAT = new DecimalFormat("0.00");

    @Inject MatchStatsCommand(UserFinder userFinder, @Sync Flexecutor flexecutor, OnlinePlayers onlinePlayers, IdentityProvider identityProvider, Audiences audiences) {
        this.userFinder = userFinder;
        this.flexecutor = flexecutor;
        this.onlinePlayers = onlinePlayers;
        this.identityProvider = identityProvider;
        this.audiences = audiences;
    }

    @Command(aliases = {"matchstats", "mstats"},
             desc = "Displays your current stats for the match",
             usage = "[target]",
             max = 1
    )
    @CommandPermissions("pgm.playerstats.matchstats")
    public List<String> matchStats(CommandContext args, CommandSender sender) throws CommandException {
        if(args.getSuggestionContext() != null) {
            if(args.getSuggestionContext().getIndex() == 0) {
                return StringUtils.complete(args.getString(0),
                                            onlinePlayers.stream().map(player -> player.getFakeDisplayName(sender)));
            }
        }

        if(args.argsLength() == 0) {
            displayStats(sender, senderToMatchPlayer(sender));
        } else {
            flexecutor.callback(
                userFinder.findLocalPlayer(sender, args, 0),
                CommandFutureCallback.onSuccess(sender, user -> displayStats(sender, senderToMatchPlayer(user.player())))
            );
        }
        return null;
    }

    private BaseComponent parseStats(MatchPlayer player) {
        StatsUserFacet facet = player.getUserContext().facet(StatsUserFacet.class);
        BaseComponent matchKills = new Component(ChatColor.GREEN).translate("command.matchstats.kills", facet.matchKills());
        BaseComponent matchDeaths = new Component(ChatColor.RED).translate("command.matchstats.deaths", facet.deaths());
        BaseComponent matchRatio = new Component(ChatColor.AQUA).translate("command.matchstats.kdr", FORMAT.format((double) facet.matchKills() / Math.max(facet.deaths(), 1)));
        return Components.join(Components.newline(), Lists.newArrayList(matchKills, matchDeaths, matchRatio));
    }
  
    private void displayStats(CommandSender sender, MatchPlayer player) {
        Audience audience = audiences.get(sender);
        audience.sendMessage(new HeaderComponent(
                             new TranslatableComponent("command.matchstats.header",
                                                       new PlayerComponent(identityProvider.createIdentity(player.getBukkit())))));
        audience.sendMessage(parseStats(player));
    }
}
