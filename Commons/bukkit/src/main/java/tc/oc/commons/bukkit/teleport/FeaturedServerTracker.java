package tc.oc.commons.bukkit.teleport;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.configuration.Configuration;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.model.ModelDispatcher;
import tc.oc.api.model.ModelListener;
import tc.oc.api.servers.ServerStore;
import tc.oc.commons.core.plugin.PluginFacet;

import static java.util.Comparator.comparing;

@Singleton
public class FeaturedServerTracker implements PluginFacet, ModelListener {

    private final Configuration config;
    private final Server localServer;
    private final ServerStore servers;

    private final Map<String, Server> featuredServersByFamily = new HashMap<>();

    // Order servers by player count, as long as they aren't full
    private final Comparator<Server> featuredServerOrder = Comparator
        .<Server>nullsLast(null)
        .thenComparing(server -> !server.online())                  // Avoid offline servers
        .thenComparing(this::isAlmostEmpty)                         // Avoid empty servers
        .thenComparing(this::isAlmostFull)                          // Avoid full servers
        .thenComparing(comparing(Server::num_online).reversed());   // Choose the fullest server

    @Inject FeaturedServerTracker(Configuration config, Server localServer, ServerStore servers, ModelDispatcher modelDispatcher) {
        this.config = config;
        this.localServer = localServer;
        this.servers = servers;
        modelDispatcher.subscribe(this);
    }

    @Override
    public void enable() {
        servers.all().forEach(this::refreshServer);
    }

    public String localDatacenter() {
        return config.getString("local-datacenter-override", localServer.datacenter());
    }

    public boolean isMappable(Server server) {
        return server != null &&
               server.alive() &&
               server.running() &&
               server.bungee_name() != null &&
               server.visibility() == ServerDoc.Visibility.PUBLIC &&
               server.datacenter().equals(localDatacenter());
    }

    public @Nullable Server featuredServerForFamily(String family) {
        return featuredServersByFamily.get(family);
    }

    /**
     * Server is "almost full" if free space is less than 10% or 3 slots, whichever is greater
     */
    public boolean isAlmostFull(Server server) {
        return server.num_participating() > Math.min(server.max_players() * 0.9, server.max_players() - 3);
    }

    public boolean isAlmostEmpty(Server server) {
        return server.num_online() <= 1;
    }

    private void refreshServer(Server changed) {
        Server featured = featuredServersByFamily.get(changed.family());
        if(changed.equals(featured)) {
            // If featured server changed, check entire family for a better server
            refreshFamily(changed.family());
        } else if(isMappable(changed) && featuredServerOrder.compare(changed, featured) < 0) {
            // Otherwise, just check if the changed server should replace the current featured one
            featuredServersByFamily.put(changed.family(), changed);
        }
    }

    private void refreshFamily(final String family) {
        final Server server = servers.first(featuredServerOrder, s -> isMappable(s) && family.equals(s.family()));
        if(server != null) {
            featuredServersByFamily.put(family, server);
        } else {
            featuredServersByFamily.remove(family);
        }
    }

    @HandleModel
    public void serverUpdated(@Nullable Server before, @Nullable Server after, Server latest) {
        refreshServer(latest);
    }
}
