package tc.oc.pgm.start;

import javax.inject.Inject;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import java.time.Duration;
import tc.oc.commons.bukkit.configuration.ConfigUtils;
import tc.oc.commons.core.util.TimeUtils;

public class StartConfig {
    private final ConfigurationSection config;

    @Inject StartConfig(Configuration root) {
        this.config = root.getConfigurationSection("start");
    }

    public boolean autoStart() {
        return config.getBoolean("auto", true);
    }

    public Duration countdown() {
        return ConfigUtils.getDuration(config, "countdown", Duration.ofSeconds(30));
    }

    public Duration huddle() {
        return ConfigUtils.getDuration(config, "huddle", Duration.ZERO);
    }

    public Duration timeout() {
        return ConfigUtils.getDuration(config, "timeout", TimeUtils.INF_POSITIVE);
    }
}
