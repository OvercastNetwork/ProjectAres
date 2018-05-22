package tc.oc.commons.bukkit.report;

import javax.inject.Inject;

import org.bukkit.configuration.Configuration;
import java.time.Duration;
import tc.oc.commons.bukkit.configuration.ConfigUtils;

public class ReportConfiguration {

    private final Configuration config;

    @Inject ReportConfiguration(Configuration config) {
        this.config = config;
    }

    public boolean enabled() {
        return config.getBoolean("reports.enabled", false);
    }

    public Duration cooldown() {
        return ConfigUtils.getDuration(config, "reports.cooldown", Duration.ZERO);
    }
}
