package tc.oc.commons.bungee.configuration;

import java.util.HashMap;
import java.util.Map;

import net.md_5.bungee.config.Configuration;

public class ConfigUtils {
    private ConfigUtils() {}

    public static Map<String, Object> toMap(Configuration config) {
        Map<String, Object> result = new HashMap<>();
        for(String key : config.getKeys()) {
            result.put(key, config.get(key));
        }
        return result;
    }

    public static Map<String, Object> toFlatMap(Configuration config) {
        Map<String, Object> result = new HashMap<>();
        if(config != null) buildFlatMap(result, null, toMap(config));
        return result;
    }

    private static void buildFlatMap(Map<String, Object> result, String path, Map<String, Object> config) {
        for(Map.Entry<String, Object> entry : config.entrySet()) {
            String key = entry.getKey();
            if(path != null) key = path + "." + key;
            final Object value = entry.getValue();

            if(value instanceof Map) {
                buildFlatMap(result, key, (Map<String, Object>) value);
            } else {
                result.put(key, value);
            }
        }
    }
}
