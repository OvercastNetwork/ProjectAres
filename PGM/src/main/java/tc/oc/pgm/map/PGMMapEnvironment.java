package tc.oc.pgm.map;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Collections2;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import tc.oc.commons.bukkit.configuration.ConfigUtils;
import tc.oc.commons.core.util.Holidays;

import static tc.oc.commons.core.util.Holidays.Holiday;
import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class PGMMapEnvironment extends ForwardingMap<String, Boolean> {

    private final ConfigurationSection config;
    private final Map<String, Boolean> manual = new HashMap<>();

    @Inject PGMMapEnvironment(Configuration config) {
        this.config = checkNotNull(config.getConfigurationSection("environment"));
    }

    @Override
    protected Map<String, Boolean> delegate() {
        return manual;
    }

    public ConfigurationSection permanent() {
        return config.getSection("permanent");
    }

    public boolean holidays() {
        return config.getBoolean("holidays", true);
    }

    @Override
    public Boolean get(Object objKey) {
        String key = (String) objKey;

        Boolean value = manual.get(key);
        if(value != null) return value;

        if(holidays()) {
            Boolean holiday = null;
            for(Holiday h : Holidays.all()) {
                if(key.equals(h.key)) {
                    holiday = h.isNow();
                    break;
                }
            }
            if(holiday != null) return holiday;
        }

        return permanent().getBoolean(key);
    }

    @Override
    public Set<String> keySet() {
        return Sets.union(Sets.union(manual.keySet(), Holidays.keys()), permanent().getKeys(false));
    }

    @Override
    public Collection<Boolean> values() {
        return Collections2.transform(keySet(), this::get);
    }

    @Override
    public Set<Entry<String, Boolean>> entrySet() {
        return ImmutableSet.copyOf(Collections2.transform(
            keySet(),
            key -> Maps.immutableEntry(key, get(key))
        ));
    }
}
