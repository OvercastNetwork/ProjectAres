package tc.oc.pgm.utils;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import tc.oc.commons.bukkit.util.Vectors;

public abstract class Locations {
    public static Location cloneWith(Location original, Vector position) {
        return new Location(original.getWorld(),
                            position.getX(),
                            position.getY(),
                            position.getZ(),
                            original.getYaw(),
                            original.getPitch());
    }

    public static Location cloneWith(Location original, float yaw, float pitch) {
        return new Location(original.getWorld(),
                            original.getX(),
                            original.getY(),
                            original.getZ(),
                            yaw,
                            pitch);
    }

    public static Location cloneWith(Location original, Vector position, float yaw, float pitch) {
        return new Location(original.getWorld(),
                            position.getX(),
                            position.getY(),
                            position.getZ(),
                            yaw,
                            pitch);
    }

    public static double horizontalDistance(Location a, Location b) {
        if(a.getWorld() != b.getWorld()) throw new IllegalArgumentException("Locations are in different worlds");
        double dx = b.getX() - a.getX();
        double dz = b.getZ() - a.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    public static String formatPosition(Location loc) {
        return Vectors.format(loc.toVector());
    }
}
