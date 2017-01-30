package tc.oc.commons.core.plugin;

import javax.annotation.Nullable;

import tc.oc.commons.core.inject.BelongsTo;

/**
 * Resolves a plugin instance from various contextual clues.
 */
public interface PluginResolver<Plugin> {

    /**
     * Return the instance of the given plugin class, if it is loaded.
     * This is not affected by the current override or fallback plugin.
     */
    @Nullable <T extends Plugin> T getPlugin(Class<T> cls);

    <T extends Plugin> T needPlugin(Class<T> cls);

    /**
     * Return the plugin that owns the given class. If the class is annotated
     * with {@link BelongsTo}, that is used to determine the owning plugin.
     * Otherwise, the class loader is checked to determine where the class
     * comes from. If a plugin cannot be determined, the fallback plugin
     * for the current thread is returned, or null if that is not set.
     * If an override plugin is set for the current thread, that is always
     * returned.
     *
     * See {@link #withFallbackPlugin} and {@link #withOverridePlugin}.
     *
     */
    @Nullable Plugin getProvidingPlugin(Class<?> cls);

    Plugin needProvidingPlugin(Class<?> cls);

    /**
     * Return the current plugin for the current thread. This is set with one of
     * the methods {@link #withFallbackPlugin} or {@link #withOverridePlugin}.
     */
    @Nullable Plugin getCurrentPlugin();

    Plugin needCurrentPlugin();

    /**
     * Execute the given block with the given plugin as the forced return
     * value from {@link #getProvidingPlugin} and {@link #getCurrentPlugin},
     * on the current thread.
     */
    void withOverridePlugin(Plugin plugin, Runnable runnable);

    /**
     * Execute the given block with the given plugin as the default
     * value for {@link #getProvidingPlugin} and {@link #getCurrentPlugin}
     * on the current thread.
     */
    void withFallbackPlugin(Plugin plugin, Runnable runnable);
}
