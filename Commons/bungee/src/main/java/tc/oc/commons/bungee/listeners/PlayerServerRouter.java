package tc.oc.commons.bungee.listeners;

import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Singleton;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import tc.oc.api.docs.Server;
import tc.oc.commons.bungee.servers.LobbyTracker;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.plugin.PluginFacet;

import static tc.oc.commons.core.stream.Collectors.toImmutableSet;

/**
 * Routes players to lobbies, and handles a few other things
 * related to switching servers.
 */
@Singleton
public class PlayerServerRouter implements Listener, PluginFacet {
    private static final String KICK_COLOR_CODES = ChatColor.BLACK.toString() + ChatColor.RED.toString();

    private final Logger logger;
    private final Server localServer;
    private final LobbyTracker lobbyTracker;

    @Inject PlayerServerRouter(Loggers loggers, Server localServer, LobbyTracker lobbyTracker) {
        this.logger = loggers.get(getClass());
        this.localServer = localServer;
        this.lobbyTracker = lobbyTracker;
    }

    @EventHandler
    public void connect(final ServerConnectEvent event) {
        // don't send "could not connect to server you're already on" message
        if(event.getPlayer().getServer() != null && event.getPlayer().getServer().getInfo().equals(event.getTarget())) {
            event.setCancelled(true);
        }

        if(event.getTarget().getName().equals("default")) {
            final int proto = event.getPlayer().getProtocolVersion();

            final Optional<ServerInfo> lobby = lobbyTracker.chooseLobby(proto);
            if(lobby.isPresent()) {
                event.setTarget(lobby.get());
            } else {
                final Set<Integer> supported = lobbyTracker.supportedProtocols().collect(toImmutableSet());
                if(!supported.isEmpty()) {
                    event.getPlayer().disconnect(
                        new Component("Please connect with Minecraft ", ChatColor.RED)
                            .extra(PingListener.describeVersionRange(supported))
                    );
                }
            }
        }

        event.setFakeUsername(localServer.fake_usernames().get(event.getPlayer().getUniqueId()));
    }

    @EventHandler
    public void reroute(final ServerKickEvent event) {
        if(event.getKickReason().contains(KICK_COLOR_CODES)) {
            event.getPlayer().disconnect(event.getKickReason());
        } else {
            event.setCancelled(true);

            if(event.getState() == ServerKickEvent.State.CONNECTED) {
                // Player was kicked off a server they were already connected to
                // Send them to the lobby, and make sure it's not the server they just left
                lobbyTracker.chooseLobby(event.getPlayer().getProtocolVersion(),
                                         event.getKickedFrom())
                            .ifPresent(event::setCancelServer);

            } else if(event.getState() == ServerKickEvent.State.CONNECTING) {
                // Player was kicked when trying to connect to a server
                if(event.getPlayer().getServer() != null) {
                    // If they are currently on a different server, keep them there
                    event.setCancelServer(event.getPlayer().getServer().getInfo());
                    for(String message : event.getKickReason().split("\n\n")) {
                        event.getPlayer().sendMessage(message);
                    }

                } else {
                    // Otherwise they were trying to connect to the network, so don't let them
                    event.getPlayer().disconnect(event.getKickReason());
                }
            }
        }
    }
}
