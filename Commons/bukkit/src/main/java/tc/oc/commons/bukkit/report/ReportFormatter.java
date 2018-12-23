package tc.oc.commons.bukkit.report;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import tc.oc.api.docs.Report;
import tc.oc.api.servers.ServerStore;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.commons.bukkit.chat.PlayerComponent;
import tc.oc.commons.bukkit.commands.CommandUtils;
import tc.oc.commons.bukkit.format.ServerFormatter;
import tc.oc.commons.bukkit.nick.IdentityProvider;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Components;
import tc.oc.commons.core.formatting.PeriodFormats;

public class ReportFormatter {

    private final IdentityProvider identityProvider;
    private final ServerStore servers;

    @Inject ReportFormatter(IdentityProvider identityProvider, ServerStore servers) {
        this.identityProvider = identityProvider;
        this.servers = servers;
    }

    public List<? extends BaseComponent> format(Report report, boolean showServer, boolean showTime) {
        final List<BaseComponent> parts = new ArrayList<>();

        parts.add(new Component(
            new Component("["),
            new Component("Rep", ChatColor.GOLD),
            new Component("]")
        ));

        if(showServer) {
            // Server may be soft-deleted, so we can't assume it's synced
            servers.tryId(report.server_id()).ifPresent(
                server -> parts.add(ServerFormatter.light.nameWithDatacenter(server))
            );
        }

        if(report.staff_online() != null) {
            final int modCount = report.staff_online().size();
            parts.add(new Component(
                new Component("("),
                new Component(modCount, modCount > 0 ? ChatColor.GREEN : ChatColor.RED),
                new Component(")")
            ));
        }

        if(showTime) {
            parts.add(new Component(PeriodFormats.relativePastApproximate(report.created_at()), ChatColor.GREEN));
        }

        if(report.reporter() != null) {
            parts.add(new PlayerComponent(identityProvider.currentIdentity(report.reporter()), NameStyle.FANCY));
        } else {
            parts.add(CommandUtils.CONSOLE_COMPONENT_NAME);
        }

        parts.add(new Component("\u2794"));

        parts.add(new Component(
            new PlayerComponent(identityProvider.currentIdentity(report.reported()), NameStyle.FANCY),
            new Component(": ")
        ));

        return ImmutableList.of(Components.join(Components.space(), parts),
                                new Component("  " + report.reason(), ChatColor.LIGHT_PURPLE));
    }
}
