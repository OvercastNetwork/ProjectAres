package tc.oc.commons.bukkit.report;

import java.util.Map;
import java.util.WeakHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import java.time.Duration;
import java.time.Instant;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.docs.Report;
import tc.oc.api.docs.Server;
import tc.oc.minecraft.scheduler.SyncExecutor;
import tc.oc.api.model.QueryService;
import tc.oc.api.reports.ReportSearchRequest;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.chat.HeaderComponent;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.commons.bukkit.chat.PlayerComponent;
import tc.oc.commons.bukkit.commands.UserFinder;
import tc.oc.commons.bukkit.nick.IdentityProvider;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.commands.CommandFutureCallback;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.commands.TranslatableCommandException;
import tc.oc.commons.core.formatting.PeriodFormats;
import tc.oc.commons.core.util.Comparables;
import tc.oc.minecraft.api.event.Listener;

@Singleton
public class ReportCommands implements Commands, Listener {

    private static final int PER_PAGE = 8;

    private final ReportFormatter reportFormatter;
    private final QueryService<Report> reportService;
    private final ReportCreator reportCreator;
    private final ReportConfiguration reportConfiguration;
    private final UserFinder userFinder;
    private final SyncExecutor syncExecutor;
    private final Server localServer;
    private final BukkitUserStore userStore;
    private final Audiences audiences;
    private final IdentityProvider identities;

    private final Map<CommandSender, Instant> senderLastReport = new WeakHashMap<>();

    @Inject ReportCommands(ReportFormatter reportFormatter,
                           QueryService<Report> reportService,
                           ReportCreator reportCreator,
                           ReportConfiguration reportConfiguration,
                           UserFinder userFinder,
                           SyncExecutor syncExecutor,
                           Server localServer,
                           BukkitUserStore userStore,
                           Audiences audiences,
                           IdentityProvider identities) {
        this.reportFormatter = reportFormatter;
        this.reportService = reportService;
        this.reportCreator = reportCreator;
        this.reportConfiguration = reportConfiguration;
        this.userFinder = userFinder;
        this.syncExecutor = syncExecutor;
        this.localServer = localServer;
        this.userStore = userStore;
        this.audiences = audiences;
        this.identities = identities;
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        senderLastReport.remove(event.getPlayer());
    }

    private void assertEnabled() throws CommandException {
        if(!reportConfiguration.enabled()) {
            throw new TranslatableCommandException("command.reports.notEnabled");
        }
    }

    @Command(
        aliases = { "report" },
        usage = "<player> <reason>",
        desc = "Report a player who is breaking the rules",
        min = 2,
        max = -1
    )
    @CommandPermissions(ReportPermissions.CREATE)
    public void report(final CommandContext args, final CommandSender sender) throws CommandException {
        assertEnabled();

        if(!sender.hasPermission(ReportPermissions.COOLDOWN_EXEMPT)) {
            final Instant lastReportTime = senderLastReport.get(sender);
            if(lastReportTime != null) {
                final Duration timeLeft = reportConfiguration.cooldown().minus(Duration.between(lastReportTime, Instant.now()));
                if(Comparables.greaterThan(timeLeft, Duration.ZERO)) {
                    throw new TranslatableCommandException("command.report.cooldown", PeriodFormats.briefNaturalApproximate(timeLeft));
                }
            }
        }

        syncExecutor.callback(
            userFinder.findLocalPlayer(sender, args, 0),
            CommandFutureCallback.onSuccess(sender, args, response -> {
                if(response.player().hasPermission(ReportPermissions.EXEMPT) && !sender.hasPermission(ReportPermissions.OVERRIDE)) {
                    throw new TranslatableCommandException("command.report.exempt", new PlayerComponent(identities.createIdentity(response)));
                }

                senderLastReport.put(sender, Instant.now());

                syncExecutor.callback(
                    reportCreator.createReport(
                        response.user,
                        sender instanceof Player ? userStore.getUser((Player) sender) : null,
                        args.getJoinedStrings(1),
                        sender instanceof ConsoleCommandSender
                    ),
                    CommandFutureCallback.onSuccess(sender, args, report -> {
                        audiences.get(sender).sendMessage(
                            new Component(
                                new Component(new TranslatableComponent("misc.thankYou"), ChatColor.GREEN),
                                new Component(" "),
                                new Component(new TranslatableComponent("command.report.successful.dealtWithMessage"), ChatColor.GOLD)
                            )
                        );
                    })
                );
            })
        );
    }

    @Command(
        aliases = { "reports", "reps" },
        usage = "[-a] [-p page] [player]",
        flags = "ap:",
        desc = "List recent reports on this server, or all servers, optionally filtering by player.",
        min = 0,
        max = 1
    )
    @CommandPermissions(ReportPermissions.VIEW)
    public void reports(final CommandContext args, final CommandSender sender) throws CommandException {
        assertEnabled();

        syncExecutor.callback(
            userFinder.findUser(sender, args.getString(0, null), UserFinder.Scope.ALL, UserFinder.Default.NULL),
            CommandFutureCallback.onSuccess(sender, args, userResult -> {
                final int page = args.getFlagInteger('p', 1);
                final boolean crossServer = args.hasFlag('a');

                ReportSearchRequest request = ReportSearchRequest.create(page, PER_PAGE);
                request = userResult != null ? request.forPlayer(userResult.user)
                                             : request.forServer(localServer, crossServer);

                syncExecutor.callback(
                    reportService.find(request),
                    CommandFutureCallback.onSuccess(sender, args, reportResult -> {
                        final Component title = new Component(new TranslatableComponent(crossServer ? "command.reports.networkTitle" : "command.reports.serverTitle"),
                                                              ChatColor.YELLOW);
                        if(userResult != null) {
                            title.extra(" (")
                                 .extra(new PlayerComponent(identities.createIdentity(userResult), NameStyle.VERBOSE))
                                 .extra(")");
                        }
                        title.extra(" ")
                             .extra(new TranslatableComponent("currentPage", String.valueOf(page)), ChatColor.DARK_AQUA);

                        final Audience audience = audiences.get(sender);
                        audience.sendMessage(new HeaderComponent(title));
                        for(Report report : reportResult.documents()) {
                            if(report.reported() != null) {
                                audience.sendMessages(reportFormatter.format(report, crossServer, true));
                            }
                        }
                    })
                );
            })
        );
    }
}
