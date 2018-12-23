package tc.oc.commons.bukkit.format;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.inject.Inject;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import tc.oc.api.docs.virtual.MatchDoc;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.minecraft.MinecraftService;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Components;
import tc.oc.commons.core.formatting.PeriodFormats;

public class ServerFormatter {

    public static Comparator<? super ServerDoc.Identity> proximityOrder(final ServerDoc.Identity server) {
        return (a, b) -> {
            int cmp = Boolean.compare(server.equals(b), server.equals(a));
            if(cmp != 0) return cmp;

            return Boolean.compare(server.datacenter().equals(b.datacenter()), server.datacenter().equals(a.datacenter()));
        };
    }

    public interface Colors {
        ChatColor online();
        ChatColor restarting();
        ChatColor offline();

        ChatColor label();

        ChatColor background();
        ChatColor maxPlayers();
        ChatColor players();
        ChatColor participants();
        ChatColor observers();

        ChatColor matchIdle();
        ChatColor matchStarting();
        ChatColor matchRunning();
        ChatColor matchFinished();

        ChatColor mapName();
    }

    // Picker and chat
    public static final Colors lightColors = new Colors() {
        @Override public ChatColor online() { return ChatColor.YELLOW; }
        @Override public ChatColor restarting() { return ChatColor.BLUE; }
        @Override public ChatColor offline() { return ChatColor.DARK_GRAY; }

        @Override public ChatColor label() { return ChatColor.BLUE; }

        @Override public ChatColor background() { return ChatColor.DARK_GRAY; }
        @Override public ChatColor maxPlayers() { return ChatColor.WHITE; }
        @Override public ChatColor players() { return ChatColor.AQUA; }
        @Override public ChatColor participants() { return ChatColor.GREEN; }
        @Override public ChatColor observers() { return ChatColor.AQUA; }

        @Override public ChatColor matchIdle() { return ChatColor.WHITE; }
        @Override public ChatColor matchStarting() { return ChatColor.GREEN; }
        @Override public ChatColor matchRunning() { return ChatColor.BLUE; }
        @Override public ChatColor matchFinished() { return ChatColor.RED; }

        @Override public ChatColor mapName() { return ChatColor.AQUA; }
    };

    // Signs
    public static final Colors darkColors = new Colors() {
        @Override public ChatColor online() { return ChatColor.BLACK; }
        @Override public ChatColor restarting() { return ChatColor.DARK_GRAY; }
        @Override public ChatColor offline() { return ChatColor.GRAY; }

        @Override public ChatColor label() { return ChatColor.DARK_BLUE; }

        @Override public ChatColor background() { return ChatColor.GRAY; }
        @Override public ChatColor maxPlayers() { return ChatColor.DARK_GRAY; }
        @Override public ChatColor players() { return ChatColor.DARK_AQUA; }
        @Override public ChatColor participants() { return ChatColor.DARK_AQUA; }
        @Override public ChatColor observers() { return ChatColor.DARK_AQUA; }

        @Override public ChatColor matchIdle() { return ChatColor.DARK_GRAY; }
        @Override public ChatColor matchStarting() { return ChatColor.BLUE; }
        @Override public ChatColor matchRunning() { return ChatColor.DARK_GREEN; }
        @Override public ChatColor matchFinished() { return ChatColor.DARK_RED; }

        @Override public ChatColor mapName() { return ChatColor.DARK_GREEN; }
    };

    @Inject static private MinecraftService minecraftService;

    // TODO: figure out an injection-friendly way to do this
    @Deprecated public static final ServerFormatter light = new ServerFormatter(lightColors);
    @Deprecated public static final ServerFormatter dark = new ServerFormatter(darkColors);

    private final Colors colors;
    public Colors colors() { return colors; }

    private ServerFormatter(Colors colors) {
        this.colors = colors;
    }

    public boolean isRestarting(ServerDoc.Listing server) {
        return server.running() && (
            !server.online() || (
                server.restart_queued_at() != null && (
                    server.current_match() == null || server.current_match().end() != null
                )
            )
        );
    }

    public ChatColor statusColor(ServerDoc.Listing server) {
        if(isRestarting(server)) {
            return colors.restarting();
        } else if(server.online()) {
            return colors.online();
        } else {
            return colors.offline();
        }
    }

    public BaseComponent name(ServerDoc.Listing server) {
        return new Component(server.name(), server.online() ? colors.online() : colors.offline(), ChatColor.BOLD);
    }

    public Optional<BaseComponent> description(ServerDoc.Listing server) {
        return server.description() == null ? Optional.empty()
                                            : Optional.of(new Component(new TranslatableComponent(server.description()), ChatColor.DARK_AQUA));
    }

    public BaseComponent onlineStatus(ServerDoc.Listing server) {
        final String key;
        if(isRestarting(server)) {
            key = "servers.restarting";
        } else if(server.online()) {
            key = "servers.online";
        } else {
            key = "servers.offline";
        }
        return new Component(new TranslatableComponent(key), statusColor(server));
    }

    public BaseComponent nameWithDatacenter(ServerDoc.Identity s) {
        return nameWithDatacenter(s.datacenter(), s.bungee_name(), s.name(), s.role() == ServerDoc.Role.LOBBY);
    }

    public BaseComponent nameWithDatacenter(@Nullable String datacenter, @Nullable String bungee, @Nullable String name, boolean lobby) {
        return nameWithDatacenter(minecraftService.getLocalServer(), datacenter, bungee, name, lobby);
    }

    public BaseComponent nameWithDatacenter(ServerDoc.Identity local, @Nullable String datacenter, @Nullable String bungee, @Nullable String name, boolean lobby) {
        Component c = new Component(ChatColor.WHITE);

        if(datacenter == null) {
            datacenter = local.datacenter();
        }

        if(!datacenter.equals(local.datacenter())) {
            c.extra("[").extra(new Component(datacenter, ChatColor.GOLD)).extra("] ");
        }

        Component nameComponent = new Component(ChatColor.GOLD);
        if(name != null) {
            nameComponent.text(name);
        } else {
            nameComponent.extra(new TranslatableComponent("servers.lobby"));
        }

        nameComponent.hoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("tip.connectTo", nameComponent.duplicate()));

        if(lobby) {
            nameComponent.clickEvent(makeLobbyClickEvent());
        } else {
            nameComponent.clickEvent(makeServerClickEvent(bungee));
        }

        c.extra("[").extra(nameComponent).extra("]");

        return c;
    }

    /**
     * [server] [player counts] [match time]
     */
    public BaseComponent compactHeading(ServerDoc.Listing server) {
        if(server.online()) {
            Component c = new Component()
                .extra(new Component(server.name(), ChatColor.WHITE, ChatColor.BOLD))
                .extra(" ")
                .extra(playerCounts(server, true));

            if(server.current_match() != null) {
                c.extra(" ").extra(matchTime(server.current_match()));
            }

            return c;
        } else {
            return new Component(ChatColor.DARK_GRAY)
                .extra(new Component(server.name(), ChatColor.BOLD))
                .extra(" (")
                .extra(new TranslatableComponent("servers.offline"))
                .extra(")");
        }
    }

    public BaseComponent playerCounts(ServerDoc.Listing server, boolean observers) {
        Component c = new Component(colors.background());

        if(server.role() == ServerDoc.Role.PGM) {
            c.extra(new Component(String.valueOf(observers ? server.num_participating() : server.num_online()), colors.participants()));
            c.extra("/");
            c.extra(new Component(String.valueOf(server.max_players()), colors.maxPlayers()));
            if(observers) {
                c.extra(" (");
                c.extra(new Component(String.valueOf(server.num_observing()), colors.observers()));
                c.extra(")");
            }
        } else {
            c.extra(new Component(String.valueOf(server.num_online()), colors.players()));
        }

        return c;
    }

    public ChatColor matchStatusColor(ServerDoc.Status server) {
        final MatchDoc match = server.current_match();

        if(match != null && server.num_online() > 0) {
            if(match.end() != null) {
                return colors.matchFinished();
            } else if(match.start() != null) {
                return colors.matchRunning();
            } else {
                return colors.matchStarting();
            }
        } else {
            return colors.matchIdle();
        }
    }

    public BaseComponent matchTime(MatchDoc doc) {
        Duration time;
        ChatColor color;
        if(doc.start() == null) {
            time = Duration.ZERO;
            color = ChatColor.GOLD;
        } else if(doc.end() == null) {
            time = Duration.between(doc.start(), Instant.now());
            color = ChatColor.GREEN;
        } else {
            time = Duration.between(doc.start(), doc.end());
            color = ChatColor.GOLD;
        }
        return new Component(PeriodFormats.formatColons(time), color);
    }

    public BaseComponent currentMap(ServerDoc.Status server) {
        final MatchDoc match = server.current_match();
        if(match == null || match.map() == null) {
            return Components.blank();
        }

        return new Component(
            new TranslatableComponent(
                "servers.currentMap",
                new Component(match.map().name(), colors.mapName())
            ),
            colors.label()
        );
    }

    public BaseComponent nextMap(ServerDoc.Status server) {
        if(server.next_map() == null) return Components.blank();

        return new Component(
            new TranslatableComponent(
                "servers.nextMap",
                new Component(server.next_map().name(), colors.mapName())
            ),
            colors.label()
        );
    }

    public ClickEvent makeLobbyClickEvent() {
        return new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/hub");
    }

    public ClickEvent makeServerClickEvent(ServerDoc.Identity server) {
        return makeServerClickEvent(server.bungee_name());
    }

    public ClickEvent makeServerClickEvent(String bungee) {
        return new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/server -b " + bungee);
    }
}
