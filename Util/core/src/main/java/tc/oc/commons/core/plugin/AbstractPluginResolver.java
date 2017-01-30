package tc.oc.commons.core.plugin;

import java.util.Set;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import tc.oc.commons.core.inject.BelongsTo;

public abstract class AbstractPluginResolver<Plugin> implements PluginResolver<Plugin> {

    /**
     * Classes excluded from implicit plugin detection (we should have a more general way to do this)
     */
    private static final Set<Class<?>> IMPLICIT_BLACKLIST = ImmutableSet.of(
        PluginFacetLoader.class
    );

    private final ThreadLocal<Plugin> override = new ThreadLocal<>();
    private final ThreadLocal<Plugin> fallback = new ThreadLocal<>();
    private final TypeToken<Plugin> pluginType = new TypeToken<Plugin>(getClass()){};

    protected abstract @Nullable Plugin getImplicitProvidingPlugin(Class<?> cls);

    private void withPlugin(ThreadLocal<Plugin> threadLocal, Plugin plugin, Runnable runnable) {
        final Plugin old = threadLocal.get();
        try {
            threadLocal.set(plugin);
            runnable.run();
        } finally {
            if(old != null) {
                threadLocal.set(old);
            } else {
                threadLocal.remove();
            }
        }
    }

    @Override
    public void withOverridePlugin(Plugin plugin, Runnable runnable) {
        withPlugin(override, plugin, runnable);
    }

    @Override
    public void withFallbackPlugin(Plugin plugin, Runnable runnable) {
        withPlugin(fallback, plugin, runnable);
    }

    @Override
    public final @Nullable Plugin getProvidingPlugin(Class<?> cls) {
        Plugin plugin = override.get();
        if(plugin != null) return plugin;

        final Class<?> owner = BelongsTo.Impl.owner(cls);
        if(owner != null && pluginType.isAssignableFrom(owner)) {
            return getPlugin((Class<? extends Plugin>) owner);
        }

        if(!IMPLICIT_BLACKLIST.contains(cls)) {
            plugin = getImplicitProvidingPlugin(cls);
            if(plugin != null) return plugin;
        }

        plugin = fallback.get();
        if(plugin != null) return plugin;

        return null;
    }

    @Override
    public @Nullable Plugin getCurrentPlugin() {
        Plugin plugin = override.get();
        if(plugin != null) return plugin;

        plugin = fallback.get();
        if(plugin != null) return plugin;

        return null;
    }

    @Override
    public <T extends Plugin> T needPlugin(Class<T> cls) {
        final T plugin = getPlugin(cls);
        if(plugin == null) {
            throw new IllegalStateException("Failed to resolve plugin type " + cls);
        }
        return plugin;
    }

    @Override
    public Plugin needProvidingPlugin(Class<?> cls) {
        final Plugin plugin = getProvidingPlugin(cls);
        if(plugin == null) {
            throw new IllegalStateException("Failed to resolve an associated plugin for type " + cls);
        }
        return plugin;
    }

    @Override
    public Plugin needCurrentPlugin() {
        final Plugin plugin = getCurrentPlugin();
        if(plugin == null) {
            throw new IllegalStateException("Failed to resolve current plugin");
        }
        return plugin;
    }
}
