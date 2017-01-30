package tc.oc.commons.bukkit.logging;

import javax.inject.Inject;

import net.kencochrane.raven.dsn.Dsn;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

public class MapdevSentryConfiguration {

    private final ConfigurationSection config;

    @Inject MapdevSentryConfiguration(Configuration config) {
        this.config = config.getSection("mapdev-sentry");
    }

    public boolean enabled() {
        return config.getBoolean("enabled", false);
    }

    public Dsn dsn() {
        return new Dsn(config.getString("dsn", null));
    }
}
