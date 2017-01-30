package tc.oc.pgm.regions;

import java.util.Random;

import org.bukkit.geometry.Cuboid;
import org.bukkit.util.ImVector;
import org.bukkit.util.Vector;

import static com.google.common.base.Preconditions.checkArgument;

public class CylindricalRegion extends Region.Impl {
    private final @Inspect ImVector center;
    private final @Inspect double bottom;
    private final @Inspect double top;
    private final @Inspect double radius;

    private final double radiusSq;

    public CylindricalRegion(Vector base, double radius, double top) {
        checkArgument(radius >= 0);
        checkArgument(Double.isFinite(base.getX()));
        checkArgument(Double.isFinite(base.getZ()));

        this.center = ImVector.of(base.getX(), 0, base.getZ());
        this.bottom = base.getY();
        this.top = top;
        this.radius = radius;
        this.radiusSq = radius * radius;
    }

    @Override
    public boolean contains(Vector point) {
        return point.getY() >= bottom &&
               point.getY() <= top &&
               center.distanceSquared(ImVector.of(point.getX(), center.getY(), point.getZ())) <= radiusSq;
    }

    @Override
    public boolean canGetRandom() {
        return isBlockBounded();
    }

    @Override
    public boolean isBlockBounded() {
        return Double.isFinite(radius) && Double.isFinite(bottom) && Double.isFinite(top);
    }

    @Override
    public Cuboid getBounds() {
        return Cuboid.between(new Vector(center.getX() - this.radius,
                                         bottom,
                                     center.getZ() - this.radius),
                              new Vector(center.getX() + this.radius,
                                     top,
                                     center.getZ() + this.radius));
    }

    @Override
    public Vector getRandom(Random random) {
        double angle = random.nextDouble() * Math.PI * 2;
        double hyp = random.nextDouble() + random.nextDouble();
        hyp = (hyp < 1D ? hyp : 2 - hyp) * radius;
        double x = Math.cos(angle) * hyp + center.getX();
        double z = Math.sin(angle) * hyp + center.getZ();
        double y = bottom + random.nextDouble() * (top - bottom);
        return new Vector(x, y, z);
    }
}
