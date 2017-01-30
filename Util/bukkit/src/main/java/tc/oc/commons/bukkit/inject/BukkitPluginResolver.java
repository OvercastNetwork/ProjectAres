package tc.oc.commons.bukkit.inject;

import javax.annotation.Nullable;
import javax.inject.Singleton;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import tc.oc.commons.core.plugin.AbstractPluginResolver;

@Singleton
public class BukkitPluginResolver extends AbstractPluginResolver<Plugin> {

    @Override
    public @Nullable <T extends Plugin> T getPlugin(Class<T> cls) {
        try {
            return (T) JavaPlugin.getPlugin(cls.asSubclass(JavaPlugin.class));
        } catch(IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public @Nullable Plugin getImplicitProvidingPlugin(Class<?> cls) {
        try {
            return JavaPlugin.getProvidingPlugin(cls);
        } catch(IllegalArgumentException e) {
            return null;
        }
    }
}
