package tc.oc.pgm.restart;

import javax.inject.Inject;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import java.time.Duration;

import static com.google.common.base.Preconditions.checkNotNull;

public class AutoRestartConfiguration {
    private final ConfigurationSection config;

    @Inject AutoRestartConfiguration(Configuration config) {
        this.config = checkNotNull(config.getConfigurationSection("autorestart"));
    }

    public boolean enabled() {
        return this.config.getBoolean("enabled", false);
    }

    public Duration time() {
        return Duration.ofSeconds(this.config.getInt("time", 30)); // seconds
    }

    public int matchLimit() {
        return this.config.getInt("match-limit", 30);
    }
}
