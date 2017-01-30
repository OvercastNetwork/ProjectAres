package tc.oc.commons.bukkit.commands;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.bukkit.users.OnlinePlayers;
import tc.oc.api.docs.User;
import tc.oc.api.exceptions.NotFound;
import tc.oc.api.minecraft.MinecraftService;
import tc.oc.api.users.UserSearchRequest;
import tc.oc.api.users.UserSearchResponse;
import tc.oc.api.users.UserService;
import tc.oc.commons.bukkit.nick.IdentityProvider;
import tc.oc.minecraft.scheduler.MainThreadExecutor;
import tc.oc.commons.bukkit.users.PlayerSearchResponse;
import tc.oc.commons.core.commands.TranslatableCommandException;
import tc.oc.commons.core.util.Orderable;

@Singleton
public class UserFinder {

    public enum Default implements Orderable<Default> { NULL, THROW, SENDER }
    public enum Scope implements Orderable<Scope> { LOCAL, ONLINE, ALL }

    private final MinecraftService minecraftService;
    private final UserService userService;
    private final BukkitUserStore userStore;
    private final IdentityProvider identityProvider;
    private final MainThreadExecutor mainThreadExecutor;
    private final OnlinePlayers onlinePlayers;

    @Inject UserFinder(MinecraftService minecraftService, UserService userService, BukkitUserStore userStore, IdentityProvider identityProvider, MainThreadExecutor mainThreadExecutor, OnlinePlayers onlinePlayers) {
        this.minecraftService = minecraftService;
        this.userService = userService;
        this.userStore = userStore;
        this.identityProvider = identityProvider;
        this.mainThreadExecutor = mainThreadExecutor;
        this.onlinePlayers = onlinePlayers;
    }

    public Player senderToPlayer(CommandSender sender) throws CommandException {
        if(sender instanceof Player) {
            return (Player) sender;
        } else {
            throw new TranslatableCommandException("command.onlyPlayers");
        }
    }

    public @Nullable User getLocalUser(CommandSender sender) {
        if(sender instanceof Player) {
            return userStore.getUser((Player) sender);
        }
        return null;
    }

    public @Nullable Player getLocalPlayer(CommandSender sender, String username) {
        return sender.getServer().getPlayerExact(username, sender);
    }

    public UserSearchResponse localUserResponse(CommandSender viewer, Player player) {
        return new UserSearchResponse(
            userStore.getUser(player),
            true,
            identityProvider.currentIdentity(player).isDisguised(viewer),
            userStore.getSession(player),
            minecraftService.getLocalServer()
        );
    }

    public PlayerSearchResponse localPlayerResponse(CommandSender viewer, Player player) {
        return new PlayerSearchResponse(
            localUserResponse(viewer, player),
            player
        );
    }

    public ListenableFuture<UserSearchResponse> findUser(final CommandSender sender, @Nullable String name, Scope scope, Default def) {
        try {
            if(name != null) {
                final Player player = getLocalPlayer(sender, name);
                if(player != null) {
                    return Futures.immediateFuture(localUserResponse(sender, player));
                }

                if(scope.noGreaterThan(Scope.LOCAL)) {
                    throw new TranslatableCommandException("command.playerNotFound");
                }

                final SettableFuture<UserSearchResponse> result = SettableFuture.create();
                Futures.addCallback(
                    userService.search(new UserSearchRequest(name, getLocalUser(sender))),
                    new FutureCallback<UserSearchResponse>() {
                        @Override
                        public void onSuccess(@Nullable UserSearchResponse response) {
                            if(!response.online && scope.noGreaterThan(Scope.ONLINE)) {
                                result.setException(new TranslatableCommandException("command.playerNotFound"));
                            } else {
                                result.set(response);
                            }
                        }

                        @Override
                        public void onFailure(Throwable e) {
                            if(e instanceof NotFound) {
                                result.setException(new TranslatableCommandException("command.playerNotFound"));
                            } else {
                                result.setException(e);
                            }
                        }
                    }
                );

                return result;
            } else {
                switch(def) {
                    case NULL:
                        return Futures.immediateFuture(null);

                    case SENDER:
                        return Futures.immediateFuture(localUserResponse(sender, senderToPlayer(sender)));

                    default:
                        throw new TranslatableCommandException("command.specifyPlayer");
                }
            }
        } catch(CommandException e) {
            return Futures.immediateFailedFuture(e);
        }
    }

    public ListenableFuture<PlayerSearchResponse> findPlayer(CommandSender sender, @Nullable String name, Scope scope, Default def) {
        try {
            final Player player = getLocalPlayer(sender, name);
            if(player != null) {
                return Futures.immediateFuture(localPlayerResponse(sender, player));
            }

            if(scope.noGreaterThan(Scope.LOCAL)) {
                throw new TranslatableCommandException("command.playerNotFound");
            }

            final SettableFuture<PlayerSearchResponse> playerResult = SettableFuture.create();
            mainThreadExecutor.callback(
                findUser(sender, name, scope, def),
                new FutureCallback<UserSearchResponse>() {
                    @Override
                    public void onSuccess(@Nullable UserSearchResponse userResult) {
                        playerResult.set(new PlayerSearchResponse(userResult, onlinePlayers.find(userResult.user)));
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        playerResult.setException(t);
                    }
                }
            );

            return playerResult;
        } catch(CommandException e) {
            return Futures.immediateFailedFuture(e);
        }
    }


    // findUser overloads

    public ListenableFuture<UserSearchResponse> findUser(final CommandSender sender, CommandContext args, int index, Scope scope, Default def) {
        return findUser(sender, args.getString(index, null), scope, def);
    }

    public ListenableFuture<UserSearchResponse> findUser(final CommandSender sender, CommandContext args, int index, Default def) {
        return findUser(sender, args, index, Scope.ALL, def);
    }

    public ListenableFuture<UserSearchResponse> findUser(final CommandSender sender, CommandContext args, int index, Scope scope) {
        return findUser(sender, args.getString(index, null), scope, Default.THROW);
    }

    public ListenableFuture<UserSearchResponse> findUser(final CommandSender sender, CommandContext args, int index) {
        return findUser(sender, args, index, Scope.ALL, Default.THROW);
    }


    // findPlayer overloads

    public ListenableFuture<PlayerSearchResponse> findPlayer(CommandSender sender, CommandContext args, int index, Scope scope, Default def) {
        return findPlayer(sender, args.getString(index, null), scope, def);
    }

    public ListenableFuture<PlayerSearchResponse> findPlayer(CommandSender sender, CommandContext args, int index, Scope scope) {
        return findPlayer(sender, args, index, scope, Default.THROW);
    }

    public ListenableFuture<PlayerSearchResponse> findPlayer(CommandSender sender, CommandContext args, int index, Default def) {
        return findPlayer(sender, args, index, Scope.ALL, def);
    }

    public ListenableFuture<PlayerSearchResponse> findPlayer(CommandSender sender, CommandContext args, int index) {
        return findPlayer(sender, args, index, Scope.ALL, Default.THROW);
    }


    // Special cases

    public ListenableFuture<PlayerSearchResponse> findLocalPlayer(CommandSender sender, CommandContext args, int index, Default def) {
        return findPlayer(sender, args, index, Scope.LOCAL, def);
    }

    public ListenableFuture<PlayerSearchResponse> findLocalPlayer(CommandSender sender, CommandContext args, int index) {
        return findLocalPlayer(sender, args, index, Default.THROW);
    }

    public ListenableFuture<PlayerSearchResponse> findLocalPlayerOrSender(CommandSender sender, CommandContext args, int index) {
        return findLocalPlayer(sender, args, index, Default.SENDER);
    }
}
