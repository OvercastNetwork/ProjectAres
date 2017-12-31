package tc.oc.commons.bukkit.format;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import java.time.Duration;
import java.time.Instant;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.Session;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.minecraft.MinecraftService;
import tc.oc.api.servers.ServerStore;
import tc.oc.api.users.UserSearchResponse;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.commons.bukkit.chat.PlayerComponent;
import tc.oc.commons.bukkit.nick.Identity;
import tc.oc.commons.bukkit.nick.IdentityProvider;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Components;
import tc.oc.commons.core.formatting.PeriodFormats;

public class UserFormatter {

    private static final Component ONLINE = new Component(new TranslatableComponent("command.lastSeen.online"), ChatColor.GREEN);

    private final IdentityProvider identityProvider;
    private final MinecraftService minecraftService;
    private final ServerStore serverStore;

    @Inject UserFormatter(IdentityProvider identityProvider, MinecraftService minecraftService, ServerStore serverStore) {
        this.identityProvider = identityProvider;
        this.minecraftService = minecraftService;
        this.serverStore = serverStore;
    }

    public BaseComponent formatLastSeen(UserSearchResponse search) {
        return formatLastSeen(search.last_session, identityProvider.createIdentity(search));
    }

    public BaseComponent formatLastSeen(Session session) {
        return formatLastSeen(session, identityProvider.createIdentity(session));
    }

    private BaseComponent formatLastSeen(@Nullable Session session, Identity identity) {
        final PlayerComponent playerComponent = new PlayerComponent(identity, NameStyle.VERBOSE);

        if(session == null) {
            return new TranslatableComponent("command.lastSeen.unknown", playerComponent);
        }

        final Server localServer = minecraftService.getLocalServer();
        final BaseComponent serverName;
        if(session.server_id() == null || localServer._id().equals(session.server_id())) {
            serverName = null;
        } else {
            Server server = serverStore.byId(session.server_id());
            if(server != null) {
                serverName = ServerFormatter.light.nameWithDatacenter(server);
            } else {
                serverName = new TranslatableComponent("misc.unknown");
            }
        }

        if(session.end() == null) {
            if(serverName != null) {
                return new TranslatableComponent("command.lastSeen.online.server", playerComponent, ONLINE, serverName);
            } else {
                return new TranslatableComponent("command.lastSeen.online.noServer", playerComponent, ONLINE);
            }
        } else {
            final Component when = new Component(PeriodFormats.briefNaturalApproximate(Duration.between(session.end(), Instant.now())), ChatColor.AQUA);

            if(serverName != null) {
                return new TranslatableComponent("command.lastSeen.offline.server", playerComponent, when, serverName);
            } else {
                return new TranslatableComponent("command.lastSeen.offline.noServer", playerComponent, when);
            }
        }
    }

    public List<BaseComponent> formatSessions(Collection<Session> sessions) {
        return formatSessions(sessions, NameStyle.FANCY);
    }

    public List<BaseComponent> formatSessions(Collection<Session> sessions, NameStyle style) {
        return formatSessions(sessions, minecraftService.getLocalServer(), style);
    }

    public List<BaseComponent> formatSessions(Collection<Session> sessions, ServerDoc.Identity localServer, NameStyle style) {
        List<BaseComponent> lines = new ArrayList<>();

        SetMultimap<Server, BaseComponent> namesByServer = HashMultimap.create();

        for(Session session : sessions) {
            if(session.end() == null) {
                final Server server = serverStore.byId(session.server_id());
                if(server != null) {
                    namesByServer.put(server, new PlayerComponent(identityProvider.createIdentity(session), style));
                }
            }
        }

        List<Server> sortedServers = new ArrayList<>(namesByServer.keySet());
        Collections.sort(sortedServers, ServerFormatter.proximityOrder(localServer));

        for(Server server : sortedServers) {
            lines.add(
                new Component()
                    .extra(ServerFormatter.light.nameWithDatacenter(server))
                    .extra(" ")
                    .extra(Components.join(new Component(" "), namesByServer.get(server)))
            );
        }

        for(Session session : sessions) {
            if(session.end() != null) {
                lines.add(formatLastSeen(session));
            }
        }

        return lines;
    }
}
