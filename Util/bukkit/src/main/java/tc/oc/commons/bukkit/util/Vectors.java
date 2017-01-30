package tc.oc.commons.bukkit.util;

import javax.annotation.Nonnull;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import tc.oc.commons.core.util.Numbers;

public class Vectors {

    public static final Vector ZERO = new Vector();
    public static final Vector NEGATIVE_INFINITY = new Vector(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
    public static final Vector POSITIVE_INFINITY = new Vector(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

    private Vectors() {}

    public static @Nonnull Vector calculateLookVector(@Nonnull Location location) {
        double pitch = Math.toRadians(location.getPitch());
        double yaw = Math.toRadians(location.getYaw());

        Vector normal = new Vector(
                -(Math.cos(pitch) * Math.sin(yaw)),
                -Math.sin(pitch),
                Math.cos(pitch) * Math.cos(yaw)
                );

        return normal;
    }

    public static String format(Vector v) {
        return format(v, "%f");
    }

    public static String format(Vector v, String format) {
        return String.format(format + ", " + format + ", " + format,
                             v.getX(), v.getY(), v.getZ());
    }

    public static Vector parseVector(String text) throws NumberFormatException {
        String[] components = text.trim().split("\\s*,\\s*");
        if(components.length != 3) {
            throw new NumberFormatException("Invalid vector '" + text + "'");
        }
        return new Vector(Numbers.parse(components[0], Double.class, true),
                          Numbers.parse(components[1], Double.class, true),
                          Numbers.parse(components[2], Double.class, true));
    }
}
