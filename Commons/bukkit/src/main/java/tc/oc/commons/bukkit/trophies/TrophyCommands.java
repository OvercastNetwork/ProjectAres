package tc.oc.commons.bukkit.trophies;

import javax.inject.Inject;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.NestedCommand;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.CommandSender;
import tc.oc.api.docs.Trophy;
import tc.oc.api.trophies.TrophyStore;
import tc.oc.api.users.UserSearchResponse;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.chat.Paginator;
import tc.oc.commons.bukkit.chat.PlayerComponent;
import tc.oc.commons.bukkit.chat.WarningComponent;
import tc.oc.commons.bukkit.commands.UserFinder;
import tc.oc.commons.bukkit.nick.Identity;
import tc.oc.commons.bukkit.nick.IdentityProvider;
import tc.oc.minecraft.scheduler.MainThreadExecutor;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.commands.CommandFutureCallback;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.commands.NestedCommands;
import tc.oc.commons.core.commands.TranslatableCommandException;

public class TrophyCommands implements NestedCommands {

    private final MainThreadExecutor executor;
    private final TrophyStore trophyStore;
    private final TrophyCase trophyCase;
    private final UserFinder userFinder;
    private final IdentityProvider identityProvider;
    private final Audiences audiences;

    @Inject TrophyCommands(MainThreadExecutor executor, TrophyStore trophyStore, TrophyCase trophyCase, UserFinder userFinder, IdentityProvider identityProvider, Audiences audiences) {
        this.executor = executor;
        this.trophyStore = trophyStore;
        this.trophyCase = trophyCase;
        this.userFinder = userFinder;
        this.identityProvider = identityProvider;
        this.audiences = audiences;
    }

    public static class Parent implements Commands {
        @Command(
                aliases = {"trophies", "trophy"},
                desc = "Commands relating to trophies."
        )
        @CommandPermissions(TrophyPermissions.BASE)
        @NestedCommand(value = TrophyCommands.class, executeBody = true)
        public void trophies(CommandContext args, CommandSender sender) throws CommandException {}
    }

    @Command(
            aliases = {"list"},
            desc = "List the trophies of a player",
            usage = "<player> <page>",
            min = 0,
            max = 2
    )
    @CommandPermissions(TrophyPermissions.LIST)
    public void list(CommandContext args, CommandSender sender) throws CommandException {
        final ListenableFuture<UserSearchResponse> future = userFinder.findUser(sender, args, 0, UserFinder.Default.SENDER);
        executor.callback(
            future,
            result -> {
                final Identity identity = identityProvider.createIdentity(result);
                final boolean self = identity.belongsTo(sender);

                executor.callback(
                    identity.isDisguised(sender) ? Futures.immediateFuture(ImmutableSet.of())
                                                 : trophyCase.getTrophies(result.user),
                    trophies -> new Paginator<Trophy>() {
                        @Override
                        protected BaseComponent title() {
                            return new TranslatableComponent(
                                self ? "trophies.list.self" : "trophies.list.other",
                                new PlayerComponent(identityProvider.createIdentity(result))
                            );
                        }
                        @Override
                        protected BaseComponent entry(Trophy entry, int index) {
                            return new Component(entry.name(), ChatColor.AQUA).extra(": ").extra(new Component(entry.description(), ChatColor.GRAY));
                        }
                    }.display(sender, trophies, args.getInteger(1, 1))
                );
            }
        );
    }

    @Command(
            aliases = {"grant"},
            desc = "Grant a trophy to a player",
            usage = "[trophy] <player>",
            min = 1,
            max = 2
    )
    @CommandPermissions(TrophyPermissions.MODIFY)
    public void grant(CommandContext args, CommandSender sender) throws CommandException {
        grantOrRevoke(args, sender, true);

    }

    @Command(
            aliases = {"revoke"},
            desc = "Revoke a trophy from a player",
            usage = "[trophy] <player>",
            min = 1,
            max = 2
    )
    @CommandPermissions(TrophyPermissions.MODIFY)
    public void revoke(CommandContext args, CommandSender sender) throws CommandException {
        grantOrRevoke(args, sender, false);
    }

    private Trophy findTrophy(String name) throws CommandException {
        return trophyStore.byName(name).orElseThrow(() -> new TranslatableCommandException("trophies.notFound", name));
    }

    private void grantOrRevoke(CommandContext args, CommandSender sender, boolean give) throws CommandException {
        final Trophy trophy = findTrophy(args.getString(0));
        executor.callback(
            userFinder.findUser(sender, args, 1, UserFinder.Default.SENDER),
            CommandFutureCallback.onSuccess(sender, args, result -> {
                executor.callback(
                    trophyCase.grantOrRevoke(result.user, trophy, give),
                    CommandFutureCallback.onSuccess(sender, args, changed -> {
                        final PlayerComponent playerComponent = new PlayerComponent(identityProvider.createIdentity(result));
                        final Component trophyComponent = new Component(trophy.name(), ChatColor.GOLD);

                        audiences.get(sender).sendMessage(
                            changed ? new TranslatableComponent(give ? "trophies.grant.success"
                                                                     : "trophies.revoke.success",
                                                                playerComponent, trophyComponent)
                                    : new WarningComponent(give ? "trophies.grant.alreadyOwns"
                                                                : "trophies.revoke.doesNotOwn",
                                                           playerComponent, trophyComponent)
                        );
                    })
                );
            })
        );
    }
}
