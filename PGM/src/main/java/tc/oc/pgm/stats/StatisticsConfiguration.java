package tc.oc.pgm.stats;

import javax.inject.Inject;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import static com.google.common.base.Preconditions.checkNotNull;

public class StatisticsConfiguration {
    private final ConfigurationSection config;

    @Inject StatisticsConfiguration(Configuration config) {
        this.config = checkNotNull(config.getConfigurationSection("stats"));
    }

    public boolean deaths() {
        return config.getBoolean("deaths", true);
    }

    public boolean objectives() {
        return config.getBoolean("objectives", true);
    }

    public boolean farming() {
        return config.getBoolean("farming", true);
    }

}
