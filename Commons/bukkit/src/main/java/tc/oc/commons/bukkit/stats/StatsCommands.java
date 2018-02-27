package tc.oc.commons.bukkit.stats;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.chat.HeaderComponent;
import tc.oc.commons.bukkit.chat.PlayerComponent;
import tc.oc.commons.bukkit.commands.UserFinder;
import tc.oc.commons.bukkit.nick.IdentityProvider;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.commands.CommandFutureCallback;
import tc.oc.commons.core.commands.Commands;
import tc.oc.minecraft.scheduler.SyncExecutor;

import javax.inject.Inject;
import java.util.HashMap;

public class StatsCommands implements Commands {

    private final SyncExecutor syncExecutor;
    private final UserFinder userFinder;
    private final Audiences audiences;
    private final IdentityProvider identityProvider;

    @Inject
    StatsCommands(UserFinder userFinder, SyncExecutor executor, Audiences audiences, IdentityProvider identityProvider) {
        this.userFinder = userFinder;
        this.syncExecutor = executor;
        this.audiences = audiences;
        this.identityProvider = identityProvider;
    }

    @Command(
            aliases = { "stats"},
            usage = "[player]",
            desc = "Shows a player's stats",
            min = 0,
            max = 1
    )
    @CommandPermissions("projectares.stats")
    public void listStats(final CommandContext args, final CommandSender sender) throws CommandException {
        syncExecutor.callback(
                userFinder.findUser(sender, args, 0, UserFinder.Default.SENDER),
                CommandFutureCallback.onSuccess(sender, args, result -> {
                    HashMap<String, Double> stats = StatsUtil.getStats(result.user);

                    Audience audience = audiences.get(sender);

                    audience.sendMessage(new HeaderComponent(new Component(ChatColor.AQUA)
                            .translate("stats.list", new PlayerComponent(identityProvider.currentIdentity(result.user)))));
                    audience.sendMessage(new Component(ChatColor.AQUA)
                            .translate("stats.kills", new Component(String.format("%,d", (int)(double)stats.get("kills")), ChatColor.BLUE)));
                    audience.sendMessage(new Component(ChatColor.AQUA)
                            .translate("stats.deaths", new Component(String.format("%,d", (int)(double)stats.get("deaths")), ChatColor.BLUE)));
                    audience.sendMessage(new Component(ChatColor.AQUA)
                            .translate("stats.kd", new Component(String.format("%.2f", stats.get("kd")), ChatColor.BLUE)));
                    audience.sendMessage(new Component(ChatColor.AQUA)
                            .translate("stats.wools", new Component(String.format("%,d", (int)(double)stats.get("wool_placed")), ChatColor.BLUE)));
                    audience.sendMessage(new Component(ChatColor.AQUA)
                            .translate("stats.cores", new Component(String.format("%,d", (int)(double)stats.get("cores_leaked")), ChatColor.BLUE)));
                    audience.sendMessage(new Component(ChatColor.AQUA)
                            .translate("stats.monuments", new Component(String.format("%,d", (int)(double)stats.get("destroyables_destroyed")), ChatColor.BLUE)));
                })
        );
    }

}
