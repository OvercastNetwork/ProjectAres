package tc.oc.pgm.regions;

import java.util.Random;

import org.bukkit.geometry.Cuboid;
import org.bukkit.util.Vector;

public class PointRegion extends Region.Impl {

    private final @Inspect Vector position;

    public PointRegion(Vector position) {
        this.position = position;
    }

    public Vector getPosition() {
        return position;
    }

    @Override
    public boolean contains(Vector point) {
        return position.equals(point);
    }

    @Override
    public Cuboid getBounds() {
        return Cuboid.between(position, position);
    }

    @Override
    public boolean isBlockBounded() {
        return true;
    }

    @Override
    public boolean canGetRandom() {
        return true;
    }

    @Override
    public Vector getRandom(Random random) {
        return new Vector(position);
    }
}
