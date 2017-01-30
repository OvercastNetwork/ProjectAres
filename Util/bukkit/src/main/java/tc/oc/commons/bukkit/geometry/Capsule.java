package tc.oc.commons.bukkit.geometry;

import org.bukkit.util.Vector;
import tc.oc.commons.core.util.Numbers;

/**
 * A 3D solid consisting of all points within a given distance from a {@link LineSegment}.
 *
 * Can also be defined as a {@link Sphere} swept along a {@link LineSegment},
 * making it useful for representing moving spheres.
 */
public class Capsule {

    private final LineSegment center;
    private final double radius, radiusSquared;

    private Capsule(LineSegment center, double radius) {
        this.center = center;
        this.radius = radius;
        this.radiusSquared = radius * radius;
    }

    public static Capsule fromCenterAndRadius(LineSegment center, double radius) {
        return new Capsule(center, radius);
    }

    public static Capsule fromEndpointsAndRadius(Vector start, Vector finish, double radius) {
        return fromCenterAndRadius(LineSegment.between(start, finish), radius);
    }

    public static Capsule fromSweptSphere(Sphere sphere, Vector delta) {
        return new Capsule(LineSegment.from(sphere.center(), delta), sphere.radius());
    }

    /**
     * Line segment at the center of the capsule
     */
    public LineSegment center() {
        return center;
    }

    /**
     * Distance from the {@link #center()} to the boundary of the capsule
     */
    public double radius() {
        return radius;
    }

    public double radiusSquared() {
        return radiusSquared;
    }

    public boolean intersects(Vector point) {
        return center.distanceSquared(point) <= radiusSquared;
    }

    public boolean intersects(Sphere sphere) {
        return center.distanceSquared(sphere.center()) <= Numbers.square(radius + sphere.radius());
    }
}
