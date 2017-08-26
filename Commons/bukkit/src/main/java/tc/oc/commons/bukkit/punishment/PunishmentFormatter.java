package tc.oc.commons.bukkit.punishment;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.CommandSender;
import tc.oc.api.docs.Punishment;
import tc.oc.api.servers.ServerStore;
import tc.oc.commons.bukkit.chat.ComponentRenderContext;
import tc.oc.commons.bukkit.chat.Links;
import tc.oc.commons.bukkit.chat.PlayerComponent;
import tc.oc.commons.bukkit.format.ServerFormatter;
import tc.oc.commons.bukkit.nick.IdentityProvider;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Components;
import tc.oc.commons.core.formatting.PeriodFormats;
import tc.oc.commons.core.util.Pair;

public class PunishmentFormatter {

    private final static Component ARROW = new Component(" \u00BB ", ChatColor.GOLD);
    private final static Component MAGIC = new Component(" \u26a0 ", ChatColor.YELLOW);

    private final IdentityProvider identityProvider;
    private final ComponentRenderContext renderContext;
    private final ServerStore servers;

    @Inject PunishmentFormatter(IdentityProvider identityProvider, ComponentRenderContext renderContext, ServerStore servers) {
        this.identityProvider = identityProvider;
        this.renderContext = renderContext;
        this.servers = servers;
    }

    public List<? extends BaseComponent> format(Punishment punishment, boolean alert, boolean server) {

        List<BaseComponent> parts = new ArrayList<>();

        if(alert) {
            parts.add(new Component("[").extra(new TranslatableComponent("punishment.prefix"), ChatColor.GOLD).extra("]"));
            if(server) {
                servers.tryId(punishment.server_id()).ifPresent(
                    s -> parts.add(ServerFormatter.light.nameWithDatacenter(s))
                );
            }
        } else {
            parts.add(new Component(PeriodFormats.relativePastApproximate(punishment.date())).extra(":"));
        }

        parts.add(new PlayerComponent(identityProvider.currentOrConsoleIdentity(punishment.punisher())));

        parts.add(new Component(new TranslatableComponent("punishment.action." + punishment.type())));

        parts.add(new Component(new PlayerComponent(identityProvider.currentOrConsoleIdentity(punishment.punished()))));

        if(punishment.expire() != null) {
            parts.add(PeriodFormats.relativeFutureApproximate(punishment.date(), punishment.expire()));
        }

        parts.get(parts.size() - 1).addExtra(":");

        return ImmutableList.of(
            Components.join(Components.space(), parts),
            new Component(" > ").extra(new Component(punishment.reason(), punishment.stale() ? ChatColor.GRAY : ChatColor.YELLOW))
        );

    }

    public Pair<BaseComponent, BaseComponent> warning(Punishment punishment) {
        return Pair.create(
            new Component(MAGIC, new Component(new TranslatableComponent("punishment.warning"), ChatColor.RED), MAGIC),
            new Component(punishment.reason(), ChatColor.AQUA)
        );
    }

    public String screen(Punishment punishment, CommandSender sender) {
        return renderContext.renderLegacy(screen(punishment), sender);
    }

    public BaseComponent screen(Punishment punishment) {

        List<BaseComponent> parts = new ArrayList<>();

        parts.add(
            new Component(
                new TranslatableComponent(
                    "punishment.screen." + punishment.type(),
                    punishment.expire() != null ? PeriodFormats.relativeFutureApproximate(punishment.date(), punishment.expire())
                                                : Components.blank()
                )
            )
        );

        parts.add(Components.blank());

        parts.add(new Component(punishment.reason(), ChatColor.YELLOW));

        parts.addAll(Components.repeat(Components::blank, 3));

        parts.add(new Component(new TranslatableComponent("punishment.screen.rules", Links.rulesLink())));

        parts.add(new Component(new TranslatableComponent("punishment.screen.appeal", Links.appealLink())));

        return Components.join(Components.newline(), parts);

    }

}
