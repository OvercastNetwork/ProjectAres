package tc.oc.pgm.teams;

import javax.inject.Inject;

import tc.oc.minecraft.api.configuration.Configuration;
import tc.oc.minecraft.api.configuration.ConfigurationSection;

import static com.google.common.base.Preconditions.checkNotNull;

public class TeamConfiguration {
    
    private final ConfigurationSection config;

    @Inject TeamConfiguration(Configuration root) {
        this.config = checkNotNull(root.getSection("teams"));
    }

    public int minimumPlayers() {
        return config.getInt("minimum-players", 0);
    }

    public boolean requireEven() {
        return config.getBoolean("even", false);
    }

    public boolean autoBalance() {
        return config.getBoolean("autobalance", true);
    }

    public boolean allowChoose() {
        return config.getBoolean("allow-choose", true);
    }

    public boolean allowSwitch() {
        return config.getBoolean("allow-switch", true);
    }
}
