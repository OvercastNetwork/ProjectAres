package tc.oc.pgm.tnt.license;

import javax.inject.Inject;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import static com.google.common.base.Preconditions.checkNotNull;

public class LicenseConfiguration {
    private final ConfigurationSection config;

    @Inject LicenseConfiguration(Configuration config) {
        this.config = checkNotNull(config.getConfigurationSection("license"));
    }

    public boolean controlAccess() {
        return config.getBoolean("control-access", false);
    }

    public boolean autoGrant() {
        return config.getBoolean("auto-grant", false);
    }

    public boolean autoRevoke() {
        return config.getBoolean("auto-revoke", false);
    }
}
