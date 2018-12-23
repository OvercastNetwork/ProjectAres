package tc.oc.commons.bukkit.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import tc.oc.commons.bukkit.logging.MapdevLogger;
import tc.oc.file.PathWatcher;
import tc.oc.file.PathWatcherService;
import tc.oc.minecraft.api.configuration.InvalidConfigurationException;
import tc.oc.minecraft.scheduler.MainThreadExecutor;

/**
 * Base class for a configuration section that is loaded from the external
 * configuration folder in the maps repo if present, otherwise from the root
 * of this plugin's configuration.
 *
 * The external file is rechecked on some interval (1 minute by default).
 */
public abstract class ExternalConfiguration {

    @Inject private MapdevLogger mapdevLogger;
    @Inject private Configuration pluginConfig;

    private ConfigurationSection config;

    @Inject void init(@Named("configuration") Path configPath, PathWatcherService watcherService, MainThreadExecutor executor) throws IOException {
        final Path path = configPath.resolve(fileName() + ".yml");
        reload(path);
        watcherService.watch(path, executor, new PathWatcher() {
            @Override
            public void fileCreated(Path path) {
                reload(path);
            }

            @Override
            public void fileModified(Path path) {
                reload(path);
            }

            @Override
            public void fileDeleted(Path path) {
                reload(path);
            }
        });
    }

    private void reload(Path file) {
        try {
            final ConfigurationSection before = config;
            final ConfigurationSection after;
            if(Files.isRegularFile(file)) {
                after = new YamlConfiguration();
                ((YamlConfiguration) after).load(file.toFile());
            } else {
                after = pluginConfig.getSection(configName());
            }
            configChanged(before, after);
            config = after; // Don't change the config if the callback throws
        } catch (IOException | InvalidConfigurationException e) {
            mapdevLogger.log(Level.SEVERE, "Error loading " + fileName() + ".yml: " + e.getMessage(), e);
        }
    }

    protected abstract String configName();

    protected String fileName() {
        return configName();
    }

    protected void configChanged(@Nullable ConfigurationSection before, @Nullable ConfigurationSection after) throws InvalidConfigurationException {}

    protected ConfigurationSection config() {
        return config;
    }
}
