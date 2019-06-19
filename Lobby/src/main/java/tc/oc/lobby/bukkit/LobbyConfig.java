package tc.oc.lobby.bukkit;

import java.util.List;
import java.util.logging.Level;
import javax.inject.Inject;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.geometry.Cuboid;
import org.bukkit.util.Vector;
import tc.oc.commons.bukkit.configuration.ConfigUtils;
import tc.oc.commons.bukkit.logging.MapdevLogger;
import tc.oc.commons.bukkit.util.Vectors;

public class LobbyConfig {

    private final Configuration config;
    private final MapdevLogger logger;

    @Inject LobbyConfig(Configuration config, MapdevLogger logger) {
        this.config = config;
        this.logger = logger;
    }

    private double getDouble(World world, String key, double def) {
        try {
            double value = (float) config.getDouble(key, Double.NaN);
            if(!Double.isNaN(value)) return value;

            String rule = world.getGameRuleValue(key);
            if(!"".equals(rule)) {
                return Double.parseDouble(rule);
            }

            return def;
        } catch(RuntimeException ex) {
            logger.log(Level.SEVERE, "Error parsing number '" + key + "' from lobby config: " + ex.getMessage(), ex);
            throw ex;
        }
    }

    private Vector getVector(World world, String key, Vector def) {
        try {
            Vector v = ConfigUtils.getVector(config, key, null);
            if(v != null) return v;

            String rule = world.getGameRuleValue(key);
            if(!"".equals(rule)) {
                return Vectors.parseVector(rule);
            }

            return def;
        } catch(RuntimeException ex) {
            logger.log(Level.SEVERE, "Error parsing vector '" + key + "' from lobby config: " + ex.getMessage(), ex);
            throw ex;
        }
    }

    public Location getSpawnLocation(World world) {
        Vector pos = getVector(world, "spawn.pos", world.getSpawnLocation().toVector());
        float yaw = (float) getDouble(world, "spawn.yaw", 0);
        float pitch = (float) getDouble(world, "spawn.pitch", 0);
        return pos.toLocation(world, yaw, pitch);
    }

    public double getSpawnRadius(World world) {
        return getDouble(world, "spawn.radius", 0);
    }

    public Cuboid getBoundaries(World world) {
        Vector min = getVector(world, "boundaries.min", Vectors.NEGATIVE_INFINITY);
        Vector max = getVector(world, "boundaries.max", Vectors.POSITIVE_INFINITY);
        return Cuboid.between(min, max);
    }

    public List<String> getDisabledPermissions() {
        return config.getStringList("disabled-permissions");
    }
}
