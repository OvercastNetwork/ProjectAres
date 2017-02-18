package tc.oc.pgm.listing;

import java.net.URL;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import javax.inject.Inject;

import tc.oc.commons.core.configuration.ConfigUtils;
import tc.oc.minecraft.api.configuration.Configuration;
import tc.oc.minecraft.api.configuration.ConfigurationSection;
import tc.oc.net.UriUtils;

class ListingConfiguration {

    private final ConfigurationSection config;

    @Inject ListingConfiguration(Configuration root) {
        this.config = root.needSection("announce");
    }

    public boolean enabled() {
        return config.getBoolean("enabled", false);
    }

    public URL announceUrl() {
        return ConfigUtils.getUrl(config, "url", UriUtils.url("https://oc.tc/announce"));
    }

    public @Nullable String serverHost() {
        return config.getString("server-host");
    }

    public OptionalInt serverPort() {
        final int port = config.getInt("server-port", 0);
        return port != 0 ? OptionalInt.of(port) : OptionalInt.empty();
    }
}
