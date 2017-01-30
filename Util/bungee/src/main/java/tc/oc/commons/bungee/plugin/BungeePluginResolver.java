package tc.oc.commons.bungee.plugin;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import tc.oc.commons.core.plugin.AbstractPluginResolver;

@Singleton
public class BungeePluginResolver extends AbstractPluginResolver<Plugin> {

    private final PluginManager pluginManager;

    @Inject BungeePluginResolver(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    @Override
    public @Nullable Plugin getImplicitProvidingPlugin(Class<?> klass) {
        final ClassLoader loader = klass.getClassLoader();
        for(Plugin plugin : pluginManager.getPlugins()) {
            if(loader == plugin.getClass().getClassLoader()) return plugin;
        }
        return null;
    }

    @Override
    public @Nullable <T extends Plugin> T getPlugin(Class<T> cls) {
        for(Plugin plugin : pluginManager.getPlugins()) {
            if(cls.isInstance(plugin)) return (T) plugin;
        }
        return null;
    }
}
