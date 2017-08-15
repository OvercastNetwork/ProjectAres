package tc.oc.commons.bukkit.nick;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.bukkit.users.OnlinePlayers;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.User;
import tc.oc.api.docs.virtual.UserDoc;
import tc.oc.api.exceptions.UnprocessableEntity;
import tc.oc.commons.core.commands.TranslatableCommandException;
import tc.oc.commons.core.formatting.PeriodFormats;
import tc.oc.minecraft.scheduler.SyncExecutor;
import tc.oc.api.users.UserService;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.commons.bukkit.chat.PlayerComponent;
import tc.oc.commons.bukkit.chat.WarningComponent;
import tc.oc.commons.bukkit.commands.CommandUtils;
import tc.oc.commons.bukkit.commands.UserFinder;
import tc.oc.commons.bukkit.event.UserLoginEvent;
import tc.oc.commons.bukkit.localization.Translations;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.commands.CommandFutureCallback;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.commands.ComponentCommandException;

@Singleton
public class NicknameCommands implements Listener, Commands {

    public static final String PERMISSION = "setting.nick";
    public static final String PERMISSION_SET = PERMISSION + ".set";
    public static final String PERMISSION_GET = PERMISSION + ".get";
    public static final String PERMISSION_IMMEDIATE = PERMISSION + ".immediate";
    public static final String PERMISSION_ANY = PERMISSION + ".any";
    public static final String PERMISSION_ANY_SET = PERMISSION_ANY + ".set";
    public static final String PERMISSION_ANY_GET = PERMISSION_ANY + ".get";
    public static final String PERMISSION_UNLIMITED = PERMISSION + ".unlimited";

    private final NicknameConfiguration config;
    private final SyncExecutor syncExecutor;
    private final BukkitUserStore userStore;
    private final UserService userService;
    private final Audiences audiences;
    private final IdentityProvider identities;
    private final OnlinePlayers onlinePlayers;
    private final UserFinder userFinder;
    private final PluginManager pluginManager;
    private final Plugin plugin;

    @Inject NicknameCommands(NicknameConfiguration config,
                             SyncExecutor syncExecutor,
                             BukkitUserStore userStore,
                             UserService userService,
                             Audiences audiences,
                             IdentityProvider identities,
                             OnlinePlayers onlinePlayers,
                             UserFinder userFinder,
                             PluginManager pluginManager,
                             Plugin plugin) {
        this.config = config;
        this.syncExecutor = syncExecutor;
        this.userStore = userStore;
        this.userService = userService;
        this.audiences = audiences;
        this.identities = identities;
        this.onlinePlayers = onlinePlayers;
        this.userFinder = userFinder;
        this.pluginManager = pluginManager;
        this.plugin = plugin;
    }

    @Override
    public void enable() {
        final PermissionAttachment attachment = Bukkit.getConsoleSender().addAttachment(plugin);
        Stream.of(
            PERMISSION,
            PERMISSION_GET,
            PERMISSION_SET,
            PERMISSION_ANY,
            PERMISSION_ANY_GET,
            PERMISSION_ANY_SET,
            PERMISSION_IMMEDIATE,
            PERMISSION_UNLIMITED
        ).forEach(name -> {
            final Permission permission = new Permission(name, PermissionDefault.FALSE);
            pluginManager.addPermission(permission);
            attachment.setPermission(permission, true);
        });
    }

    private static boolean isSelf(CommandSender sender, @Nullable String username) {
        return username == null || username.equals(sender.getName());
    }

    private void assertWritePerms(CommandSender sender, boolean self, boolean immediate) throws CommandException {
        if(self) {
            CommandUtils.assertPermission(sender, PERMISSION_SET);
        } else {
            CommandUtils.assertPermission(sender, PERMISSION_ANY_SET);
        }

        if(immediate) {
            CommandUtils.assertPermission(sender, PERMISSION_IMMEDIATE);
        }

        if(sender instanceof Player) {
            Instant updatedAt = userStore.getUser((Player) sender).nickname_updated_at();
            Instant nextAt = updatedAt == null ? Instant.now() : updatedAt.plus(Duration.ofDays(1));
            if(!sender.hasPermission(PERMISSION_UNLIMITED) && nextAt.isAfter(Instant.now())) {
                throw new TranslatableCommandException(
                    "command.nick.mustWait",
                    PeriodFormats.relativeFutureApproximate(nextAt)
                );
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void sendNickReminderOnLogin(UserLoginEvent event) {
        if(event.getUser().nickname() != null) {
            if(event.getPlayer().hasPermission(PERMISSION_SET)) {
                audiences.get(event.getPlayer()).sendMessage(new TranslatableComponent(
                    "nick.joinReminder",
                    new Component("/nick", ChatColor.GOLD),
                    new Component("/nick clear", ChatColor.GOLD)
                ));
            } else {
                set(event.getUser(), null, true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void sendNickChangeMessage(PlayerIdentityChangeEvent event) {
        if(event.getNewIdentity().getNickname() == null) {
            audiences.get(event.getPlayer()).sendMessage(new TranslatableComponent("command.nick.clearSelf.immediate"));
        } else {
            audiences.get(event.getPlayer()).sendMessage(new TranslatableComponent("command.nick.setSelf.immediate", highlight(event.getNewIdentity().getNickname())));
        }
    }

    @Command(aliases = {"nick" },
             usage = "list | show [player] | set <nickname> [player] | clear [player] | <nickname> [player]",
             desc = "Show, set, or clear a nickname for yourself or another player. " +
                    "Changes will take effect the next time the player " +
                    "connects to the server. The -i option makes the change " +
                    "visible immediately.",
             flags = "i",
             min = 0,
             max = 3)
    @CommandPermissions(PERMISSION)
    public void nick(final CommandContext args, final CommandSender sender) throws CommandException {
        if(!config.enabled()) {
            throw new CommandException(Translations.get().t("command.nick.notEnabled", sender));
        }

        final boolean immediate = args.hasFlag('i');

        if(args.argsLength() == 0) {
            show(sender, null);
        } else {
            final String arg = args.getString(0);
            switch(arg) {
                case "list":
                    list(sender);
                    break;

                case "show":
                    show(sender, args.getString(1, null));
                    break;

                case "set":
                    if(args.argsLength() < 2) CommandUtils.notEnoughArguments(sender);
                    set(sender, args.getString(1), args.getString(2, null), immediate);
                    break;

                case "clear":
                    set(sender, null, args.getString(1, null), immediate);
                    break;

                default:
                    set(sender, arg, args.getString(1, null), immediate);
                    break;
            }
        }
    }

    public void list(final CommandSender sender) throws CommandException {
        CommandUtils.assertPermission(sender, PERMISSION_ANY_GET);

        final Audience audience = audiences.get(sender);
        boolean some = false;

        for(Player player : onlinePlayers.all()) {
            final Identity identity = identities.currentIdentity(player);
            if(identity.getNickname() != null) {
                some = true;
                audience.sendMessage(new PlayerComponent(identity, NameStyle.VERBOSE));
            }
        }

        if(!some) {
            audience.sendMessage(new TranslatableComponent("command.nick.noActiveNicks"));
        }
    }

    public void show(final CommandSender sender, final @Nullable String username) throws CommandException {
        final boolean self = isSelf(sender, username);
        final Audience audience = audiences.get(sender);

        syncExecutor.callback(
            userFinder.findUser(sender, username, UserFinder.Scope.ALL, UserFinder.Default.SENDER),
            CommandFutureCallback.onSuccess(sender, result -> {
                final Identity identity = identities.currentIdentity(result.user);

                if(self || identity.isFriend(sender)) {
                    CommandUtils.assertPermission(sender, PERMISSION_GET);
                } else {
                    CommandUtils.assertPermission(sender, PERMISSION_ANY_GET);
                }

                final String currentNick = identity.getNickname();
                final String pendingNick = result.user.nickname();

                final String who = self ? "Self" : "Other";
                final PlayerComponent name = self ? null : new PlayerComponent(identity, NameStyle.FANCY);

                TranslatableComponent message = null;

                if(currentNick != null) {
                    message = new TranslatableComponent("command.nick.set" + who + ".immediate", highlight(currentNick));
                } else if(pendingNick == null) {
                    message = new TranslatableComponent("command.nick.clear" + who + ".immediate");
                }

                if(message != null) {
                    if(name != null) message.addWith(name);
                    audience.sendMessage(message);
                }

                if(!Objects.equals(currentNick, pendingNick)) {
                    if(pendingNick != null) {
                        message = new TranslatableComponent("command.nick.set" + who + ".queued", highlight(pendingNick));
                    } else {
                        message = new TranslatableComponent("command.nick.clear" + who + ".queued");
                    }

                    if(name != null) message.addWith(name);
                    audience.sendMessage(message);
                }
            })
        );
    }

    private static Component highlight(String nickname) {
        return new Component(nickname, ChatColor.AQUA);
    }

    public @Nullable BaseComponent invalidReason(@Nullable String nickname) {
        if(nickname == null) return null;

        if(!nickname.matches("^[A-Za-z0-9_]+$")) {
            return new TranslatableComponent("command.nick.invalidCharacters");
        }
        if(nickname.length() > 16) {
            return new TranslatableComponent("command.nick.tooLong");
        }
        if(nickname.length() < 4) {
            return new TranslatableComponent("command.nick.tooShort");
        }
        if(onlinePlayers.find(nickname) != null) {
            return new TranslatableComponent("command.nick.nickTaken", highlight(nickname));
        }
        return null;
    }

    private void validate(@Nullable String nickname) throws CommandException {
        final BaseComponent reason = invalidReason(nickname);
        if(reason != null) {
            throw new ComponentCommandException(reason);
        }
    }

    public void set(final CommandSender sender, final @Nullable String nickname, final @Nullable String username, final boolean immediate) throws CommandException {
        final boolean self = isSelf(sender, username);

        // Don't need perms to clear your own nickname, only to set it
        if(!(self && nickname == null)) {
            assertWritePerms(sender, self, immediate);
        }

        final Audience audience = audiences.get(sender);

        if(nickname != null) {
            validate(nickname);
            audience.sendMessage(new TranslatableComponent("command.nick.checkingNickname", highlight(nickname)));
        }

        // TODO: a way to do this with only one API call instead of two
        syncExecutor.callback(
            userFinder.findUser(sender, username, UserFinder.Scope.ALL, UserFinder.Default.SENDER),
            CommandFutureCallback.onSuccess(sender, response -> {
                syncExecutor.callback(
                    set(response.user, nickname, immediate),
                    CommandFutureCallback.<User>onSuccess(sender, user -> {
                        if(!(self && immediate)) {
                            final String key = "command.nick." +
                                               (nickname == null ? "clear" : "set") +
                                               (self ? "Self" : "Other") +
                                               (immediate ? ".immediate" : ".queued");

                            final TranslatableComponent message = new TranslatableComponent(key);
                            if(nickname != null) message.addWith(highlight(nickname));
                            if(!self) message.addWith(new PlayerComponent(identities.createIdentity(user, null), NameStyle.FANCY));

                            audience.sendMessage(message);
                        }
                    }).onFailure(UnprocessableEntity.class, ex -> {
                        // Assume any validation error is a username collision
                        audience.sendMessage(new WarningComponent("command.nick.nickTaken", highlight(nickname)));
                    })
                );
            })
        );
    }

    public ListenableFuture<User> set(PlayerId playerId, @Nullable String nickname, boolean immediate) {
        final BaseComponent reason = invalidReason(nickname);
        if(reason != null) {
            return Futures.immediateFailedFuture(new ComponentCommandException(reason));
        }

        return Futures.transform(
            userService.update(playerId, (UserDoc.Nickname) () -> nickname),
            (Function<User, User>) user -> {
                if(immediate) {
                    final Player player = onlinePlayers.find(user);
                    if(player != null) {
                        identities.changeIdentity(player, nickname);
                    }
                }
                return user;
            },
            syncExecutor
        );
    }
}
