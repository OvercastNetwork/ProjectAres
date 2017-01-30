package tc.oc.commons.bukkit.geometry;

import org.bukkit.geometry.Cuboid;
import org.bukkit.util.ImVector;
import org.bukkit.util.Vector;

/**
 * Sphere defined by center point and radius
 */
public class Sphere {

    private final ImVector center;
    private final double radius, radiusSquared;

    private Sphere(ImVector center, double radius) {
        this.center = center;
        this.radius = radius;
        this.radiusSquared = radius * radius;
    }

    public static Sphere fromCenterAndRadius(Vector center, double radius) {
        return new Sphere(ImVector.copyOf(center), radius);
    }

    public static Sphere fromCircumscribedCuboid(Cuboid cuboid) {
        return fromCenterAndRadius(cuboid.center(), cuboid.maximum().distance(cuboid.center()));
    }

    public ImVector center() {
        return center;
    }

    public double radius() {
        return radius;
    }

    public double radiusSquared() {
        return radiusSquared;
    }
}
