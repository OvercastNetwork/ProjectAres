package tc.oc.commons.core.logging;

import java.util.logging.Logger;
import javax.annotation.Nullable;

import tc.oc.commons.core.plugin.PluginResolver;

public abstract class PluginLoggerFactory<Plugin> extends ClassLoggerFactory {

    private final PluginResolver<Plugin> resolver;

    protected PluginLoggerFactory(PluginResolver<Plugin> resolver) {
        this.resolver = resolver;
    }

    protected abstract Logger pluginLogger(Plugin plugin);

    @Override
    public Logger defaultLogger(Class<?> klass) {
        final Plugin plugin = resolver.getProvidingPlugin(klass);
        return plugin != null ? pluginLogger(plugin) : defaultLogger();
    }
}
