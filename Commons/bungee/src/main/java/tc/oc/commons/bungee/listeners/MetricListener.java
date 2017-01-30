package tc.oc.commons.bungee.listeners;

import java.net.InetSocketAddress;
import javax.inject.Inject;
import javax.inject.Singleton;

import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import tc.oc.api.servers.ServerService;
import tc.oc.api.servers.BungeeMetricRequest;
import tc.oc.commons.core.plugin.PluginFacet;

@Singleton
public class MetricListener implements Listener, PluginFacet {

    private final ServerService serverService;

    @Inject MetricListener(ServerService serverService) {
        this.serverService = serverService;
    }

    @EventHandler
    public void ping(final ProxyPingEvent event) {
        doBungeMetric(event.getConnection().getAddress(), BungeeMetricRequest.Type.PING);
    }

    @EventHandler
    public void join(final PostLoginEvent event) {
        doBungeMetric(event.getPlayer().getAddress(), BungeeMetricRequest.Type.LOGIN);
    }

    private void doBungeMetric(InetSocketAddress address, BungeeMetricRequest.Type type) {
        serverService.doBungeeMetric(new BungeeMetricRequest(address.getAddress().getHostAddress(), type));
    }
}
