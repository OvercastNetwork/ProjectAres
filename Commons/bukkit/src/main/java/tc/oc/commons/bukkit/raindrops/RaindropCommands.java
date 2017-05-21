package tc.oc.commons.bukkit.raindrops;

import javax.inject.Inject;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.minecraft.scheduler.SyncExecutor;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.chat.PlayerComponent;
import tc.oc.commons.bukkit.commands.UserFinder;
import tc.oc.commons.bukkit.nick.IdentityProvider;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.commands.CommandFutureCallback;
import tc.oc.commons.core.commands.Commands;

/**
 * General raindrop related commands
 */
public class RaindropCommands implements Commands {

    private final UserFinder userFinder;
    private final SyncExecutor executor;
    private final Audiences audiences;
    private final IdentityProvider identityProvider;

    @Inject RaindropCommands(UserFinder userFinder, SyncExecutor executor, Audiences audiences, IdentityProvider identityProvider) {
        this.userFinder = userFinder;
        this.executor = executor;
        this.audiences = audiences;
        this.identityProvider = identityProvider;
    }

    @Command(
        aliases = {"droplets", "drp"},
        usage = "[player]",
        desc = "Shows the amount of droplets that you have",
        min = 0,
        max = 1
    )
    public void raindrops(final CommandContext args, final CommandSender sender) throws CommandException {
        executor.callback(
            userFinder.findUser(sender, args, 0, UserFinder.Default.SENDER),
            CommandFutureCallback.onSuccess(sender, args, result -> {
                final boolean self = sender instanceof Player && ((Player) sender).getUniqueId().equals(result.user.uuid());
                final int raindrops;
                if(result.disguised && result.last_session != null) {
                    // Generate a pseudo-random amount of raindrops between ~1000 and 100,000
                    raindrops = 10000000 / (100 + Math.abs(result.last_session.nickname().hashCode() % 9900));
                } else {
                    raindrops = result.user.raindrops();
                }
                audiences.get(sender).sendMessage(
                    new Component(ChatColor.WHITE)
                        .translate(self ? "raindrops.balance.self" : "raindrops.balance.other",
                                   new PlayerComponent(identityProvider.createIdentity(result)),
                                   new Component(String.format("%,d", raindrops), ChatColor.AQUA),
                                   new TranslatableComponent(Math.abs(raindrops) == 1 ? "raindrops.singular" : "raindrops.plural"))
                );
            })
        );
    }
}
