package tc.oc.commons.core.configuration;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.time.Duration;
import tc.oc.commons.core.util.Predicates;
import tc.oc.commons.core.util.TimeUtils;
import tc.oc.minecraft.api.configuration.ConfigurationSection;

public final class ConfigUtils {
    private ConfigUtils() {}

    public static List<String> getStringList(ConfigurationSection section, String path, List<String> def) {
        final Collection<String> v = (Collection<String>) section.get(path);
        return v != null ? ImmutableList.copyOf(v) : def;
    }

    public static Set<String> getStringSet(ConfigurationSection section, String path, Set<String> def) {
        final Collection<String> v = (Collection<String>) section.get(path);
        return v != null ? ImmutableSet.copyOf(v) : def;
    }

    public static Set<String> getStringSet(ConfigurationSection section, String path) {
        return getStringSet(section, path, Collections.emptySet());
    }

    public static Predicate<String> getStringSetPredicate(ConfigurationSection section, String path, Predicate<String> def) {
        Set<String> set = getStringSet(section, path, null);
        return set != null ? set::contains : def;
    }

    public static Predicate<String> getStringSetPredicate(ConfigurationSection section, String path) {
        return getStringSetPredicate(section, path, Predicates.alwaysFalse());
    }

    public static Duration getDuration(ConfigurationSection section, String path, Duration def) {
        return TimeUtils.parseDuration(section.getString(path), def);
    }

    public static @Nullable Duration getDuration(ConfigurationSection section, String path) {
        return getDuration(section, path, null);
    }

    private static void buildDeepMap(Map<String, Object> map, ConfigurationSection section, String prefix) {
        for(String key : section.getKeys()) {
            final Object obj = section.get(key);
            if(obj instanceof ConfigurationSection || obj instanceof Map) {
                buildDeepMap(map, section.getSection(key), prefix + key + ".");
            } else {
                map.put(prefix + key, obj);
            }
        }
    }

    public static Map<String, Object> asMap(ConfigurationSection section, boolean deep) {
        Map<String, Object> map = new HashMap<>();
        if(deep) {
            buildDeepMap(map, section, "");
        } else {
            for(String key : section.getKeys()) {
                map.put(key, section.get(key));
            }
        }
        return map;
    }
}
