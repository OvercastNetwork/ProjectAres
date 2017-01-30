package tc.oc.commons.bungee.servers;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import net.md_5.bungee.api.config.ServerInfo;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.model.ModelListener;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.commons.core.stream.BiStream;

import static java.util.stream.Collectors.toSet;

/**
 * Track available lobbies and choose the best lobby to receive players at any given time
 */
@Singleton
public class LobbyTracker implements ModelListener, PluginFacet {

    private final Logger logger;
    private final Server localServer;
    private final ServerTracker serverTracker;

    protected final Map<Server, ServerInfo> activeLobbies = new ConcurrentHashMap<>();

    @Inject LobbyTracker(Loggers loggers, Server localServer, ServerTracker serverTracker) {
        this.logger = loggers.get(getClass());
        this.localServer = localServer;
        this.serverTracker = serverTracker;
    }

    /**
     * Return the set of protocol versions supported by at least one active lobby
     */
    public Stream<Integer> supportedProtocols() {
        return activeLobbies.keySet()
                            .stream()
                            .flatMap(server -> server.protocol_versions().stream());
    }

    /**
     * Choose the best lobby to join with the given protocol
     */
    public Optional<ServerInfo> chooseLobby(int protocol) {
        return chooseLobby(protocol, null);
    }

    /**
     * Choose the best lobby to join with the given protocol,
     * besides the given excluded lobby.
     */
    public Optional<ServerInfo> chooseLobby(int protocol, @Nullable ServerInfo excluded) {
        if(activeLobbies.isEmpty()) {
            logger.severe("No active lobbies");
            return Optional.empty();
        }

        return BiStream.from(activeLobbies)
                       .filterKeys(server -> server.protocol_versions().contains(protocol))
                       .filterValues(info -> !Objects.equals(excluded, info))
                       .maxByKey(Comparator.comparing(Server::num_online))
                       .map(Map.Entry::getValue);
    }

    @HandleModel
    public void serverUpdated(@Nullable Server before, @Nullable Server after, Server latest) {
        register(latest);
    }

    private void register(Server server) {
        final Optional<ServerInfo> info = Optional.of(server)
                                                  .filter(this::isActiveLobby)
                                                  .flatMap(serverTracker::serverInfo);
        if(info.isPresent()) {
            if(activeLobbies.put(server, info.get()) == null) {
                logger.fine("Added lobby " + server.bungee_name());
            }
        } else {
            if(activeLobbies.remove(server) != null) {
                logger.fine("Removed lobby " + server.bungee_name());
            }
        }
    }

    private boolean isActiveLobby(Server server) {
        return server.role() == ServerDoc.Role.LOBBY &&
               server.restart_queued_at() == null &&
               localServer.datacenter().equals(server.datacenter());
    }
}
