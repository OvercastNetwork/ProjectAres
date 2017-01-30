package tc.oc.pgm.join;

import javax.inject.Inject;

import tc.oc.minecraft.api.configuration.Configuration;
import tc.oc.minecraft.api.configuration.ConfigurationSection;

import static com.google.common.base.Preconditions.checkNotNull;

public class JoinConfiguration {

    private final ConfigurationSection config;

    @Inject JoinConfiguration(Configuration root) {
        this.config = checkNotNull(root.getSection("join"));
    }

    public boolean priorityKick() {
        return config.getBoolean("priority-kick", true);
    }

    public boolean midMatch() {
        return config.getBoolean("mid-match", true);
    }

    public boolean commitPlayers() {
        return config.getBoolean("commit-players", false);
    }

    public boolean capacity() {
        return config.getBoolean("capacity.enabled", false);
    }

    public boolean overfill() {
        return config.getBoolean("capacity.overfill", false);
    }

    public double overfillRatio() {
        return Math.max(1, config.getDouble("capacity.overfill-ratio", 1.25));
    }

    public int overfillFromMax(int max) {
        return overfill() ? (int) (max * overfillRatio())
                          : max;
    }
}
