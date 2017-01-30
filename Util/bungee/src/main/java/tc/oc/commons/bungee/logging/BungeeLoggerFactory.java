package tc.oc.commons.bungee.logging;

import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Singleton;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import tc.oc.commons.core.logging.PluginLoggerFactory;
import tc.oc.commons.core.plugin.PluginResolver;

@Singleton
public class BungeeLoggerFactory extends PluginLoggerFactory<Plugin> {

    private final ProxyServer proxy;

    @Inject BungeeLoggerFactory(PluginResolver<Plugin> resolver, ProxyServer proxy) {
        super(resolver);
        this.proxy = proxy;
    }

    @Override
    protected Logger pluginLogger(Plugin plugin) {
        return plugin.getLogger();
    }

    @Override
    public Logger defaultLogger() {
        return proxy.getLogger();
    }
}
