package tc.oc.pgm.map;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.collect.ImmutableSet;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import tc.oc.commons.bukkit.configuration.ConfigUtils;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.minecraft.api.configuration.InvalidConfigurationException;

public class PGMMapConfiguration implements MapConfiguration {

    private final Logger logger;
    private final ConfigurationSection config;
    private final PGMMapEnvironment environment;
    private final Path serverRoot;

    @Inject PGMMapConfiguration(Loggers loggers, Configuration root, PGMMapEnvironment environment, @Named("serverRoot") Path serverRoot) {
        this.logger = loggers.get(getClass());
        this.config = root.getSection("map");
        this.environment = environment;
        this.serverRoot = serverRoot;
    }

    @Override
    public List<Path> includePaths() {
        return ConfigUtils.getPathList(config, "include-path");
    }

    @Override
    public List<Path> globalIncludes() {
        return ConfigUtils.getPathList(config, "global-includes");
    }

    @Override
    public Map<String, Boolean> environment() {
        return environment;
    }

    @Override
    public boolean autoReload() {
        return config.getBoolean("autoreload.enabled", true);
    }

    @Override
    public boolean reloadWhenError() {
        return config.getBoolean("autoreload.reload-when-error", false);
    }

    @Override
    public List<MapSource> sources() {
        logger.fine("Loading map sources...");

        final List<MapSource> sources = new ArrayList<>();
        final ConfigurationSection sourcesSection = config.getSection("sources");

        for(String key : sourcesSection.getKeys(false)) {
            try {
                sources.add(loadSource(key, sourcesSection.needSection(key)));
            } catch (InvalidConfigurationException e) {
                logger.warning("Failed to parse maps source: " + e.getMessage());
            }
        }

        Collections.sort(sources);
        logger.fine("Loaded " + sources.size() + " sources");
        return sources;
    }

    protected MapSource loadSource(String key, ConfigurationSection section) throws InvalidConfigurationException {
        Path sourcePath = ConfigUtils.getPath(section, "path", null);
        if(sourcePath != null && !sourcePath.isAbsolute()) {
            sourcePath = serverRoot.resolve(sourcePath);
        }
        if(sourcePath == null || !Files.isDirectory(sourcePath)) {
            throw new InvalidConfigurationException("Skipping '" + key + "' because it does not have a valid path");
        }

        final MapSource source = new MapSource(key,
                                               sourcePath,
                                               ConfigUtils.getUrl(section, "url", null),
                                               section.getInt("depth", Integer.MAX_VALUE),
                                               ImmutableSet.copyOf(ConfigUtils.getPathList(section, "only")),
                                               ImmutableSet.copyOf(ConfigUtils.getPathList(section, "exclude")),
                                               section.getInt("priority", 0),
                                               section.getBoolean("global-includes", true));
        logger.fine("  " + source);
        return source;
    }
}
