package tc.oc.commons.bukkit.logging;

import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import tc.oc.commons.core.logging.PluginLoggerFactory;
import tc.oc.commons.core.plugin.PluginResolver;

@Singleton
public class BukkitLoggerFactory extends PluginLoggerFactory<Plugin> {

    private final Server server;

    @Inject BukkitLoggerFactory(PluginResolver<Plugin> resolver, Server server) {
        super(resolver);
        this.server = server;
    }

    @Override
    protected Logger pluginLogger(Plugin plugin) {
        return plugin.getLogger();
    }

    @Override
    public Logger defaultLogger() {
        return server.getLogger();
    }
}
