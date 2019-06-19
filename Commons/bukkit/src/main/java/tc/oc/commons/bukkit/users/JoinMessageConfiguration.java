package tc.oc.commons.bukkit.users;

import java.util.function.Predicate;
import javax.inject.Inject;
import tc.oc.commons.core.configuration.ConfigUtils;
import tc.oc.commons.core.util.Predicates;
import tc.oc.minecraft.api.configuration.Configuration;
import tc.oc.minecraft.api.configuration.ConfigurationSection;

public class JoinMessageConfiguration {

    private final ConfigurationSection config;

    @Inject JoinMessageConfiguration(Configuration config) {
        this.config = config.getSection("join-messages");
    }

    /**
     * Show join/leave/change messages
     */
    public boolean enabled() {
        return config.getBoolean("enabled", true);
    }

    /**
     * Show messages from other server networks
     */
    public boolean crossNetwork() {
        return config.getBoolean("cross-network", true);
    }

    /**
     * Show messages only from servers in the given families
     */
    public Predicate<String> families() {
        return ConfigUtils.getStringSetPredicate(config, "families", Predicates.alwaysTrue());
    }


    /**
     * Show messages only from servers in the given realms
     */
    public Predicate<String> realms() {
        return ConfigUtils.getStringSetPredicate(config, "realms", Predicates.alwaysTrue());
    }
}
