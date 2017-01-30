package tc.oc.pgm.freeze;

import javax.inject.Inject;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import static com.google.common.base.Preconditions.checkNotNull;

public class FreezeConfig {
    private final ConfigurationSection config;

    @Inject FreezeConfig(Configuration root) {
        this.config = checkNotNull(root.getConfigurationSection("freeze"), "Missing freeze configuration section");
    }

    public boolean enabled() {
        return config.getBoolean("enabled", false);
    }

    public double tntVictimRadius() {
        return config.getDouble("remove-tnt.victim-radius", -1);
    }

    public double tntSenderRadius() {
        return config.getDouble("remove-tnt.sender-radius", -1);
    }
}
