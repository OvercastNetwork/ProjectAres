package tc.oc.pgm.regions;

import org.bukkit.geometry.Cuboid;
import org.bukkit.util.ImVector;
import org.bukkit.util.Vector;

import static com.google.common.base.Preconditions.checkArgument;

public class SphereRegion extends Region.Impl {
    protected final @Inspect ImVector center;
    protected final @Inspect double radius;
    protected final double radiusSq;

    public SphereRegion(Vector center, double radius) {
        checkArgument(radius >= 0);

        this.center = ImVector.copyOf(center);
        this.radius = radius;
        this.radiusSq = radius * radius;
    }

    public double getRadius() {
        return this.radius;
    }

    public double getRadiusSquared() {
        return this.radiusSq;
    }

    @Override
    public boolean contains(Vector point) {
        return this.center.distanceSquared(point) <= this.radiusSq;
    }

    @Override
    public boolean isBlockBounded() {
        return !Double.isInfinite(radius);
    }

    @Override
    public Cuboid getBounds() {
        Vector diagonal = new Vector(this.radius, this.radius, this.radius);
        return Cuboid.between(new Vector(center).subtract(diagonal),
                              new Vector(center).add(diagonal));
    }
}
