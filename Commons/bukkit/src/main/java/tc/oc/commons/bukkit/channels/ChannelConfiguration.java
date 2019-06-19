package tc.oc.commons.bukkit.channels;

import javax.inject.Inject;
import org.bukkit.configuration.Configuration;

public class ChannelConfiguration {

    private final Configuration config;

    @Inject ChannelConfiguration(Configuration config) {
        this.config = config;
    }

    public boolean admin_enabled() {
        return config.getBoolean("channels.admin.enabled", false);
    }

    public boolean admin_cross_server() {
        return config.getBoolean("channels.admin.cross-server", false);
    }
}
