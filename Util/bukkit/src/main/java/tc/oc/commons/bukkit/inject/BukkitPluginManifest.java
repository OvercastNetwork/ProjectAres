package tc.oc.commons.bukkit.inject;

import java.io.File;
import java.nio.file.Path;
import javax.inject.Named;

import com.google.inject.Provides;
import org.bukkit.plugin.Plugin;
import tc.oc.commons.bukkit.commands.BukkitCommandManifest;
import tc.oc.commons.bukkit.scheduler.BukkitSchedulerManifest;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.plugin.MinecraftPluginManifest;
import tc.oc.commons.core.plugin.PluginFacetManifest;

public class BukkitPluginManifest extends HybridManifest {

    @Override
    protected void configure() {
        publicBinder().install(new BukkitServerManifest());
        install(new MinecraftPluginManifest());
        install(new PluginFacetManifest());
        install(new BukkitSchedulerManifest());
        install(new BukkitCommandManifest());

        bind(tc.oc.minecraft.api.configuration.Configuration.class).to(org.bukkit.configuration.Configuration.class);
    }

    @Provides @Named("pluginData")
    File pluginDataFile(Plugin plugin) {
        return plugin.getDataFolder();
    }

    @Provides @Named("pluginData")
    Path pluginDataPath(Plugin plugin) {
        return pluginDataFile(plugin).toPath();
    }
}
