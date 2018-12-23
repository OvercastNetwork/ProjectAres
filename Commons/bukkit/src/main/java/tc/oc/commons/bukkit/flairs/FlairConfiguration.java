package tc.oc.commons.bukkit.flairs;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

public class FlairConfiguration {

    private final ConfigurationSection config;

    @Inject
    FlairConfiguration(Configuration config) {
        this.config = checkNotNull(config.getConfigurationSection("flairs"));
    }

    public boolean overheadFlair() {
        return config.getBoolean("overhead", false);
    }

    public int maxFlairs() {
        return config.getInt("limit", -1);
    }
}
