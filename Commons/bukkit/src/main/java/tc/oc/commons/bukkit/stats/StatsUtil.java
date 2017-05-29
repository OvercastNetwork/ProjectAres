package tc.oc.commons.bukkit.stats;

import org.bukkit.entity.Player;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.docs.User;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class StatsUtil {

    @Inject
    private static BukkitUserStore userStore;

    public static HashMap<String, Double> getStats(Player player) {
        return getStats(userStore.getUser(player));
    }

    public static HashMap<String, Double> getStats(User user) {
        HashMap<String, Double> map = new HashMap<>();

        if (user.stats_value() != null) {
            Map<String, Map<String, Object>> statsCheck = user.stats_value().get("eternity");
            if (statsCheck != null) {
                Map<String, Object> stats = statsCheck.get("global");
                if (stats != null) {
                    map.put("kills", getDoubleValue(stats.get("kills")));
                    map.put("deaths", getDoubleValue(stats.get("deaths")));
                    map.put("kd", getDoubleValue(stats.get("kd")));
                    map.put("kk", getDoubleValue(stats.get("kk")));
                    map.put("wool_placed", getDoubleValue(stats.get("wool_placed")));
                    map.put("cores_leaked", getDoubleValue(stats.get("cores_leaked")));
                    map.put("destroyables_destroyed", getDoubleValue(stats.get("destroyables_destroyed")));
                    map.put("tkrate", getDoubleValue(stats.get("tkrate")));

                    return map;
                }
            }
        }
        map.put("kills", 0.0);
        map.put("deaths", 0.0);
        map.put("kd", 0.0);
        map.put("kk", 0.0);
        map.put("wool_placed", 0.0);
        map.put("cores_leaked", 0.0);
        map.put("destroyables_destroyed", 0.0);
        map.put("tkrate", 0.0);

        return map;
    }

    private static Double getDoubleValue(Object value) {
        if (value == null) {
            return 0.0;
        }
        return (Double)value;
    }

}
