package tc.oc.commons.bungee;

import tc.oc.api.model.ModelListenerBinder;
import tc.oc.commons.bungee.commands.ServerCommands;
import tc.oc.commons.bungee.inject.BungeePluginManifest;
import tc.oc.commons.bungee.listeners.LoginListener;
import tc.oc.commons.bungee.listeners.MetricListener;
import tc.oc.commons.bungee.listeners.PingListener;
import tc.oc.bungee.analytics.BungeePlayerReporter;
import tc.oc.bungee.analytics.PlayerTimeoutReporter;
import tc.oc.commons.bungee.listeners.PlayerServerRouter;
import tc.oc.commons.bungee.listeners.TeleportListener;
import tc.oc.commons.bungee.restart.RestartListener;
import tc.oc.commons.bungee.servers.LobbyTracker;
import tc.oc.commons.bungee.servers.ServerTracker;
import tc.oc.commons.bungee.sessions.MojangSessionServiceCommands;
import tc.oc.commons.bungee.sessions.MojangSessionServiceMonitor;
import tc.oc.commons.core.CommonsCoreManifest;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.plugin.PluginFacetBinder;

public class CommonsBungeeManifest extends HybridManifest {
    @Override
    protected void configure() {
        install(new CommonsCoreManifest());
        install(new BungeePluginManifest());

        bindAndExpose(ServerTracker.class);
        bindAndExpose(LobbyTracker.class);

        final ModelListenerBinder models = new ModelListenerBinder(publicBinder());
        models.bindListener().to(ServerTracker.class);
        models.bindListener().to(LobbyTracker.class);

        final PluginFacetBinder facets = new PluginFacetBinder(binder());
        facets.register(ServerTracker.class);
        facets.register(LobbyTracker.class);
        facets.register(LoginListener.class);
        facets.register(MetricListener.class);
        facets.register(MojangSessionServiceCommands.class);
        facets.register(MojangSessionServiceMonitor.class);
        facets.register(PingListener.class);
        facets.register(RestartListener.class);
        facets.register(ServerCommands.class);
        facets.register(PlayerServerRouter.class);
        facets.register(TeleportListener.class);

        // DataDog
        facets.register(BungeePlayerReporter.class);
        facets.register(PlayerTimeoutReporter.class);
    }
}
