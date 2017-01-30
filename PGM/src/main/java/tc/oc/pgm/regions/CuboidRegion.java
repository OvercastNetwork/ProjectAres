package tc.oc.pgm.regions;

import java.util.Random;

import org.bukkit.geometry.Cuboid;
import org.bukkit.util.ImVector;
import org.bukkit.util.Vector;

public class CuboidRegion extends Region.Impl {
    private final Cuboid bounds;

    public CuboidRegion(Vector pos1, Vector pos2) {
        this.bounds = Cuboid.between(Vector.getMinimum(pos1, pos2), Vector.getMaximum(pos1, pos2));
    }

    @Inspect
    public ImVector min() {
        return bounds.minimum();
    }

    @Inspect
    public ImVector max() {
        return bounds.maximum();
    }

    @Override
    public boolean contains(Vector point) {
        return this.bounds.contains(point);
    }

    @Override
    public boolean canGetRandom() {
        return bounds.isFinite();
    }

    @Override
    public boolean isBlockBounded() {
        return bounds.isFinite();
    }

    @Override
    public Cuboid getBounds() {
        return this.bounds;
    }

    @Override
    public Vector getRandom(Random random) {
        if(this.bounds.isEmpty()) {
            throw new ArithmeticException("Region is empty");
        }

        double x = this.randomRange(random, this.bounds.minimum().getX(), this.bounds.maximum().getX());
        double y = this.randomRange(random, this.bounds.minimum().getY(), this.bounds.maximum().getY());
        double z = this.randomRange(random, this.bounds.minimum().getZ(), this.bounds.maximum().getZ());
        return new Vector(x, y, z);
    }

    private double randomRange(Random random, double min, double max) {
        return (max - min) * random.nextDouble() + min;
    }
}
