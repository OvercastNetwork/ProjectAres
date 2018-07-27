package tc.oc.commons.bukkit.punishment;

import java.time.Duration;
import java.util.List;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Collections2;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.CommandSender;
import tc.oc.api.docs.Punishment;
import tc.oc.api.docs.User;
import tc.oc.api.model.QueryService;
import tc.oc.api.punishments.PunishmentSearchRequest;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.chat.Paginator;
import tc.oc.commons.bukkit.chat.PlayerComponent;
import tc.oc.commons.bukkit.chat.WarningComponent;
import tc.oc.commons.bukkit.commands.CommandUtils;
import tc.oc.commons.bukkit.commands.UserFinder;
import tc.oc.commons.bukkit.nick.IdentityProvider;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.concurrent.Flexecutor;
import tc.oc.minecraft.scheduler.Sync;

import static tc.oc.api.docs.virtual.PunishmentDoc.Type;
import static tc.oc.api.docs.virtual.PunishmentDoc.Type.*;
import static tc.oc.commons.bukkit.commands.UserFinder.Default.NULL;
import static tc.oc.commons.bukkit.commands.UserFinder.Default.SENDER;
import static tc.oc.commons.bukkit.commands.UserFinder.Scope;
import static tc.oc.commons.bukkit.punishment.PunishmentPermissions.fromFlag;
import static tc.oc.commons.bukkit.punishment.PunishmentPermissions.fromType;

@Singleton
public class PunishmentCommands implements Commands {

    private final QueryService<Punishment> punishmentService;
    private final PunishmentCreator punishmentCreator;
    private final PunishmentFormatter punishmentFormatter;
    private final PunishmentEnforcer punishmentEnforcer;
    private final UserFinder userFinder;
    private final IdentityProvider identityProvider;
    private final Audiences audiences;
    private final Flexecutor syncExecutor;

    @Inject PunishmentCommands(QueryService<Punishment> punishmentService,
                               PunishmentCreator punishmentCreator,
                               PunishmentFormatter punishmentFormatter,
                               PunishmentEnforcer punishmentEnforcer,
                               UserFinder userFinder,
                               IdentityProvider identityProvider,
                               Audiences audiences,
                               @Sync Flexecutor syncExecutor) {

        this.punishmentService = punishmentService;
        this.punishmentCreator = punishmentCreator;
        this.punishmentFormatter = punishmentFormatter;
        this.punishmentEnforcer = punishmentEnforcer;
        this.userFinder = userFinder;
        this.identityProvider = identityProvider;
        this.audiences = audiences;
        this.syncExecutor = syncExecutor;
    }

    public boolean flag(char flag, CommandContext args, CommandSender sender) throws CommandException {
        if(args.hasFlag(flag)) {
            if(sender.hasPermission(fromFlag(flag))) {
                return true;
            } throw new CommandPermissionsException();
        }
        return false;
    }

    public boolean permission(CommandSender sender, @Nullable Type type) throws CommandException {
        if(!sender.hasPermission(fromType(type))) {
            throw new CommandPermissionsException();
        }
        return true;
    }

    public void create(CommandContext args, CommandSender sender, @Nullable Type type, @Nullable Duration duration) throws CommandException {
        final User punisher = userFinder.getLocalUser(sender);
        final String reason = args.getJoinedStrings(duration == null ? 1 : 2);
        final boolean auto = flag('a', args, sender);
        final boolean silent = flag('s', args, sender);
        final boolean offrecord = flag('o', args, sender);
        final Scope scope = punishmentCreator.offRecord() ? Scope.LOCAL : Scope.ALL;
        if(permission(sender, type)) {
            syncExecutor.callback(
                userFinder.findUser(sender, args, 0, scope),
                response -> {
                    punishmentCreator.create(
                        punisher,
                        response.user,
                        reason,
                        type,
                        duration,
                        silent,
                        auto,
                        offrecord
                    );
                }
            );
        }
    }

    @Command(
        aliases = { "w", "warn" },
        flags = "aso",
        usage = "<player> <reason>",
        desc = "Warn a player for a reason.",
        min = 2
    )
    public void warn(CommandContext args, CommandSender sender) throws CommandException {
        create(args, sender, WARN, null);
    }

    @Command(
        aliases = { "k", "kick" },
        flags = "aso",
        usage = "<player> <reason>",
        desc = "Kick a player for a reason.",
        min = 2
    )
    public void kick(CommandContext args, CommandSender sender) throws CommandException {
        create(args, sender, KICK, null);
    }

    @Command(
        aliases = { "tb", "tempban" },
        flags = "aso",
        usage = "<player> <duration> <reason>",
        desc = "Temporarily ban a player for a reason.",
        min = 3
    )
    public void tempban(CommandContext args, CommandSender sender) throws CommandException {
        create(args, sender, BAN, CommandUtils.getDuration(args, 1, Duration.ofDays(7)));
    }

    @Command(
        aliases = { "pb", "permaban" },
        flags = "aso",
        usage = "<player> <reason>",
        desc = "Permanently ban a player for a reason.",
        min = 2
    )
    public void permaban(CommandContext args, CommandSender sender) throws CommandException {
        create(args, sender, BAN, null);
    }

    @Command(
        aliases = { "p", "punish" },
        flags = "aso",
        usage = "<player> <reason>",
        desc = "Punish a player for a reason.",
        min = 2
    )
    public void punish(CommandContext args, CommandSender sender) throws CommandException {
        create(args, sender, null, null); // Website will handle choosing which punishment
    }

    @Command(
        aliases = { "rp", "repeatpunish" },
        usage = "[player]",
        desc = "Show the last punishment you issued or repeat it for a different player",
        min = 0,
        max = 1
    )
    public void repeat(CommandContext args, CommandSender sender) throws CommandException {
        final User punisher = userFinder.getLocalUser(sender);
        final Audience audience = audiences.get(sender);
        if(permission(sender, null)) {
            syncExecutor.callback(
                    userFinder.findUser(sender, args, 0, NULL),
                    punished -> syncExecutor.callback(
                            punishmentService.find(PunishmentSearchRequest.punisher(punisher, 1)),
                            punishments -> punishments.documents().stream().findFirst().<Runnable>map(last -> () -> {
                                if (punished != null) {
                                    punishmentCreator.repeat(last, punished.user);
                                } else {
                                    audience.sendMessages(punishmentFormatter.format(last, false, false));
                                }
                            }).orElse(() -> audience.sendMessage(new WarningComponent("punishment.noneIssued"))).run()
                    )
            );
        }
    }

    @Command(
        aliases = { "l", "lookup" },
        usage = "<player>",
        desc = "Lookup previous punishments for player.",
        min = 0,
        max = 1
    )
    public void lookup(CommandContext args, CommandSender sender) throws CommandException {
        syncExecutor.callback(
            userFinder.findUser(sender, args, 0, SENDER),
            user -> {
                syncExecutor.callback(
                    punishmentService.find(PunishmentSearchRequest.punished(user.user, true, null)),
                    punishments -> {
                        new Paginator<Punishment>() {
                            @Override
                            protected BaseComponent title() {
                                return new TranslatableComponent(
                                    "punishment.lookup",
                                    new PlayerComponent(identityProvider.createIdentity(user))
                                );
                            }
                            @Override
                            protected List<? extends BaseComponent> multiEntry(Punishment entry, int index) {
                                return punishmentFormatter.format(entry, false, false);
                            }
                        }.display(
                            sender,
                            Collections2.filter(punishments.documents(), p -> punishmentEnforcer.viewable(sender, p, false)),
                            args.getInteger(1, 1)
                        );
                    }
                );
            }
        );
    }

}
