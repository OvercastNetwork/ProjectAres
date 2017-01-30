package tc.oc.commons.bukkit.geometry;

import org.bukkit.Location;
import org.bukkit.util.ImVector;
import org.bukkit.util.Vector;

public class Direction {

    private static final double TWO_PI = Math.PI * 2, HALF_PI = Math.PI / 2;

    private final double yaw, pitch;

    private Direction(double yaw, double pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public double yaw() {
        return yaw;
    }

    public double pitch() {
        return pitch;
    }

    public double yawDegrees() {
        return Math.toDegrees(yaw);
    }

    public double pitchDegrees() {
        return Math.toDegrees(pitch);
    }

    public ImVector toVector() {
        final double cos = Math.cos(pitch);
        return ImVector.of(-cos * Math.sin(yaw),
                           -Math.sin(pitch),
                                   cos * Math.cos(yaw));
    }

    public static Direction of(double yaw, double pitch) {
        return new Direction((yaw + TWO_PI) % TWO_PI, pitch);
    }

    public static Direction fromDegrees(double yaw, double pitch) {
        return of(Math.toRadians(yaw), Math.toRadians(pitch));
    }

    public static Direction fromLocation(Location location) {
        return fromDegrees(location.getYaw(), location.getPitch());
    }

    public static Direction fromVector(Vector dir) {
        return fromVector(dir.getX(), dir.getY(), dir.getZ());
    }

    public static Direction fromVector(double x, double y, double z) {
        if (x == 0 && z == 0) {
            return new Direction(0, y > 0 ? -HALF_PI : HALF_PI);
        }

        final double xz = Math.sqrt(x * x + z * z);
        return of(Math.atan2(-x, z), Math.atan(-y / xz));
    }
}
