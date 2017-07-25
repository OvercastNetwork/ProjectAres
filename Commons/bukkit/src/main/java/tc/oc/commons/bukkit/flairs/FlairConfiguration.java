package tc.oc.commons.bukkit.flairs;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;

public class FlairConfiguration {

    private final ConfigurationSection config;

    @Inject
    FlairConfiguration(Configuration config) {
        this.config = checkNotNull(config.getConfigurationSection("flairs"));
    }

    public boolean overheadFlair() {
        return config.getBoolean("overhead-flair", false);
    }

    public int maxFlairs() {
        return config.getInt("max-flairs", -1);
    }
}
