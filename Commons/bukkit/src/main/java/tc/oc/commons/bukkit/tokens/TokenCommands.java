package tc.oc.commons.bukkit.tokens;

import com.sk89q.minecraft.util.commands.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.chat.PlayerComponent;
import tc.oc.commons.bukkit.commands.UserFinder;
import tc.oc.commons.bukkit.nick.IdentityProvider;
import tc.oc.commons.bukkit.raindrops.RaindropUtil;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.commands.CommandFutureCallback;
import tc.oc.commons.core.commands.Commands;
import tc.oc.minecraft.scheduler.SyncExecutor;

import javax.inject.Inject;

public class TokenCommands implements Commands {
    private final UserFinder userFinder;
    private final SyncExecutor executor;
    private final Audiences audiences;
    private final IdentityProvider identityProvider;

    @Inject
    TokenCommands(UserFinder userFinder, SyncExecutor executor, Audiences audiences, IdentityProvider identityProvider) {
        this.userFinder = userFinder;
        this.executor = executor;
        this.audiences = audiences;
        this.identityProvider = identityProvider;
    }

    @Command(
            aliases = {"tokens"},
            usage = "[player]",
            desc = "Shows the amount of tokens that you have",
            min = 0,
            max = 1
    )
    public void tokens(final CommandContext args, final CommandSender sender) throws CommandException {
        executor.callback(
                userFinder.findUser(sender, args, 0, UserFinder.Default.SENDER),
                CommandFutureCallback.onSuccess(sender, args, result -> {
                    final boolean self = sender instanceof Player && ((Player) sender).getUniqueId().equals(result.user.uuid());
                    final int mapTokens = result.user.maptokens();
                    audiences.get(sender).sendMessage(
                            new Component(ChatColor.WHITE)
                                    .translate(self ? "maptokens.balance.self" : "maptokens.balance.other",
                                            new PlayerComponent(identityProvider.createIdentity(result)),
                                            new Component(String.format("%,d", mapTokens), ChatColor.AQUA),
                                            new TranslatableComponent(Math.abs(mapTokens) == 1 ? "maptokens.singular" : "maptokens.plural"))
                    );
                    final int mutationTokens = result.user.mutationtokens();
                    audiences.get(sender).sendMessage(
                            new Component(ChatColor.WHITE)
                                    .translate(self ? "mutationtokens.balance.self" : "mutationtokens.balance.other",
                                            new PlayerComponent(identityProvider.createIdentity(result)),
                                            new Component(String.format("%,d", mutationTokens), ChatColor.AQUA),
                                            new TranslatableComponent(Math.abs(mutationTokens) == 1 ? "mutationtokens.singular" : "mutationtokens.plural"))
                    );
                })
        );
    }


    @Command(
            aliases = {"givetokens"},
            usage = "[player] [setnext|mutation] [count]",
            desc = "gives a player tokens",
            min = 3,
            max = 3
    )
    @CommandPermissions("tokens.give")
    public void givetokens(final CommandContext args, final CommandSender sender) throws CommandException {
        int numberOfTokens = args.getInteger(2);
        executor.callback(
                userFinder.findUser(sender, args, 0, UserFinder.Default.SENDER),
                CommandFutureCallback.onSuccess(sender, args, result -> {
                    result.user.player_id();
                    String type = args.getString(1).toLowerCase();
                    if (type.equals("setnext") || type.equals("map")) {
                        TokenUtil.giveMapTokens(result.user, numberOfTokens);
                    } else if (type.equals("mutation") || type.equals("mt")) {
                        TokenUtil.giveMutationTokens(result.user, numberOfTokens);
                    } else if (type.equals("droplets") || type.equals("raindrops") || type.equals("rds")) {
                        RaindropUtil.giveRaindrops(result.user, numberOfTokens, null);
                    } else {
                        throw new CommandUsageException(ChatColor.RED + "/givetokens [player] [setnext|mutation] [count]");
                    }
                })
        );
    }
}
