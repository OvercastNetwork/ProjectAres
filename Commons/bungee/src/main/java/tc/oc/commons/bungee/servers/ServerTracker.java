package tc.oc.commons.bungee.servers;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.model.ModelListener;
import tc.oc.api.servers.ServerStore;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.plugin.PluginFacet;

/**
 * Keeps Bungee's list of {@link ServerInfo}s in sync with the {@link Server}s
 * in the {@link ServerStore}, and provides conversions between them.
 */
@Singleton
public class ServerTracker implements ModelListener, PluginFacet {

    protected final Logger logger;
    protected final ProxyServer proxy;
    protected final boolean interDatacenter;
    protected final Server localServer;
    protected final Provider<ServerStore> serverStore; // Circular dependency

    @Inject ServerTracker(Loggers loggers, Configuration configuration, ProxyServer proxy, Server localServer, Provider<ServerStore> serverStore) {
        this.localServer = localServer;
        this.logger = loggers.get(getClass());
        this.proxy = proxy;
        this.interDatacenter = configuration.getBoolean("inter-datacenter", true);
        this.serverStore = serverStore;
    }

    /**
     * Return the {@link Server} document for the given proxy record
     */
    public Server byInfo(ServerInfo info) {
        return serverStore.get().byBungeeName(info.getName());
    }

    /**
     * Return the {@link Server} document for the server that the given player is currently connected to
     */
    public Server byPlayer(ProxiedPlayer player) {
        return byInfo(player.getServer().getInfo());
    }

    /**
     * If the given {@link Server} is connectable from this proxy, return it's {@link ServerInfo}
     */
    public Optional<ServerInfo> serverInfo(@Nullable ServerDoc.Identity server) {
        final Server complete = serverStore.get().byId(server._id());
        if(isConnectable(complete)) {
            ServerInfo info = proxy.getServerInfo(server.bungee_name());
            if(!isInfoCorrect(complete, info)) {
                logger.fine("Registering " + server.bungee_name());
                info = proxy.constructServerInfo(
                    server.bungee_name(),
                    new InetSocketAddress(complete.ip(), complete.current_port()),
                    "",
                    false
                );
                proxy.getConfig().addServer(info);
            }
            return Optional.of(info);
        } else {
            if(proxy.getConfig().removeServerNamed(server.bungee_name()) != null) {
                logger.fine("Unregistering " + server.bungee_name());
            }
            return Optional.empty();
        }
    }

    private boolean isConnectable(Server server) {
        return server.alive() &&
               server.online() &&
               server.ip() != null &&
               server.current_port() != null &&
               server.bungee_name() != null && server.bungee_name().length() != 0 &&
               (Objects.equals(server.datacenter(), localServer.datacenter()) || interDatacenter);
    }

    private boolean isInfoCorrect(Server server, @Nullable ServerInfo info) {
        return info != null &&
               server.ip().equals(info.getAddress().getHostString()) &&
               server.current_port().equals(info.getAddress().getPort());
    }

    @HandleModel
    public void serverUpdated(@Nullable Server before, @Nullable Server after, Server latest) {
        serverInfo(latest);
    }
}
