package tc.oc.analytics.datadog;

import javax.inject.Inject;

import tc.oc.minecraft.api.configuration.Configuration;
import tc.oc.minecraft.api.configuration.ConfigurationSection;
import tc.oc.minecraft.api.configuration.InvalidConfigurationException;

class DataDogConfig {

    private final ConfigurationSection section;

    @Inject DataDogConfig(Configuration config) throws InvalidConfigurationException {
        this.section = config.getSection("datadog");
    }

    public boolean enabled() {
        return section.getBoolean("enabled", false);
    }

    public String host() {
        return section.getString("host", "localhost");
    }

    public int port() {
        return section.getInt("port", 8125);
    }
}
