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

    public static final Vector rotateAroundAxisX(Vector v, double rad) {
        double y, z, cos, sin;
        cos = Math.cos(rad);
        sin = Math.sin(rad);
        y = v.getY() * cos - v.getZ() * sin;
        z = v.getY() * sin + v.getZ() * cos;
        return v.setY(y).setZ(z);
    }

    public static final Vector rotateAroundAxisY(Vector v, double rad) {
        double x, z, cos, sin;
        cos = Math.cos(rad);
        sin = Math.sin(rad);
        x = v.getX() * cos + v.getZ() * sin;
        z = v.getX() * -sin + v.getZ() * cos;
        return v.setX(x).setZ(z);
    }

    public static final Vector rotateAroundAxisZ(Vector v, double rad) {
        double x, y, cos, sin;
        cos = Math.cos(rad);
        sin = Math.sin(rad);
        x = v.getX() * cos - v.getY() * sin;
        y = v.getX() * sin + v.getY() * cos;
        return v.setX(x).setY(y);
    }

    public static final Vector rotateVector(Vector v, double radX, double radY, double radZ) {
        rotateAroundAxisX(v, radX);
        rotateAroundAxisY(v, radY);
        rotateAroundAxisZ(v, radZ);
        return v;
    }

    /**
     * Rotate a vector about a location using that location's direction
     *
     * @param v
     * @param location
     * @return
     */
    public static final Vector rotateVector(Vector v, Location location) {
        double yaw = Math.toRadians(-1 * (location.getYaw() + 90));
        double pitch = Math.toRadians(-location.getPitch());
        return rotateVector(v, pitch, yaw);
    }

    /**
     * This handles non-unit vectors, with yaw and pitch instead of X,Y,Z angles.
     *
     * Thanks to SexyToad!
     *
     * @param v
     * @param pitchRadians
     * @param yawRadians
     * @return
     */
    public static final Vector rotateVector(Vector v, double pitchRadians, double yawRadians) {
        double cosYaw = Math.cos(yawRadians);
        double cosPitch = Math.cos(pitchRadians);
        double sinYaw = Math.sin(yawRadians);
        double sinPitch = Math.sin(pitchRadians);

        double initialX, initialY, initialZ;
        double x, y, z;

        // Z_Axis rotation (Pitch)
        initialX = v.getX();
        initialY = v.getY();
        x = initialX * cosPitch - initialY * sinPitch;
        y = initialX * sinPitch + initialY * cosPitch;

        // Y_Axis rotation (Yaw)
        initialZ = v.getZ();
        initialX = x;
        z = initialZ * cosYaw - initialX * sinYaw;
        x = initialZ * sinYaw + initialX * cosYaw;

        return v.setX(x).setY(y).setZ(z);
    }

    public static final double angleToXAxis(Vector vector) {
        return Math.atan2(vector.getX(), vector.getY());
    }
}
