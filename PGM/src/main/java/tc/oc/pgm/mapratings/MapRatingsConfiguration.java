package tc.oc.pgm.mapratings;

import javax.inject.Inject;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

public class MapRatingsConfiguration {
    private final ConfigurationSection config;

    @Inject MapRatingsConfiguration(Configuration config) {
        this.config = config.getSection("map-ratings");
    }

    public boolean enabled() {
        return config.getBoolean("enabled", true);
    }
}
