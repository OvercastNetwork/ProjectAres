package tc.oc.pgm.quota;

import java.util.NavigableSet;
import java.util.TreeSet;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Range;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import java.time.Duration;
import tc.oc.commons.bukkit.configuration.ConfigUtils;
import tc.oc.pgm.match.MatchPlayer;

public class QuotaConfig {
    private final @Nullable ConfigurationSection config;
    private final NavigableSet<Entry> quotas;

    @Inject QuotaConfig(Configuration root) {
        this.config = root.getConfigurationSection("match-quotas");
        if(config == null) {
            quotas = new TreeSet<>();
        } else {
            quotas = new TreeSet<>(Collections2.transform(
                config.getKeys(false),
                new Function<String, Entry>() {
                    @Override
                    public Entry apply(@Nullable String key) {
                        return new Entry(config.getConfigurationSection(key));
                    }
                }
            ));
        }
    }

    public Iterable<? extends Quota> getQuotas() {
        return quotas;
    }

    public @Nullable Range<Integer> getPremiumMaximum() {
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for(Quota quota : getQuotas()) {
            if(quota.premium()) {
                min = Math.min(min, quota.maximum());
                max = Math.max(max, quota.maximum());
            }
        }
        return min <= max ? Range.closed(min, max) : null;
    }

    private class Entry implements Quota, Comparable<Entry> {
        private final ConfigurationSection config;

        private Entry(ConfigurationSection config) {
            this.config = config;
            validate();
        }

        public void validate() {
            priority();
            interval();
            maximum();
        }

        @Override
        public int priority() {
            return config.getInt("priority");
        }

        @Override
        public int compareTo(Entry o) {
            return Integer.compare(priority(), o.priority());
        }

        private @Nullable String permission() {
            return config.getString("permission");
        }

        @Override
        public boolean appliesTo(MatchPlayer player) {
            String perm = permission();
            return perm == null || player.getBukkit().hasPermission(perm);
        }

        @Override
        public Duration interval() {
            return ConfigUtils.getDuration(config, "interval");
        }

        @Override
        public int maximum() {
            int max = config.getInt("max");
            if(max < 1) throw new IllegalArgumentException("Quota max must be at least 1");
            return max;
        }

        @Override
        public boolean premium() {
            return config.getBoolean("premium", false);
        }
    }
}
