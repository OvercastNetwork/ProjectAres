package tc.oc.commons.bukkit.nick;

import javax.inject.Inject;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import static com.google.common.base.Preconditions.checkNotNull;

public class NicknameConfiguration {

    private final ConfigurationSection config;

    @Inject NicknameConfiguration(Configuration config) {
        this.config = checkNotNull(config.getConfigurationSection("nicks"));
    }

    public boolean enabled() {
        return config.getBoolean("enabled", false);
    }

    public boolean overheadFlair() {
        // This doesn't strike me as particularly nickname related
        return config.getBoolean("overhead-flair", false);
    }
}
