package tc.oc.pgm.regions;

import org.bukkit.geometry.Cuboid;
import org.bukkit.util.Vector;

import java.util.Random;

public class BlockRegion extends Region.Impl {
    protected final @Inspect Vector location;

    public BlockRegion(Vector block) {
        this.location = new Vector(block.getBlockX(), block.getBlockY(), block.getBlockZ());
    }

    @Override
    public boolean contains(Vector point) {
        return this.location.getBlockX() == point.getBlockX() &&
                this.location.getBlockY() == point.getBlockY() &&
                this.location.getBlockZ() == point.getBlockZ();
    }

    @Override
    public boolean canGetRandom() {
        return true;
    }

    @Override
    public boolean isBlockBounded() {
        return true;
    }

    @Override
    public Cuboid getBounds() {
        return Cuboid.between(this.location, this.location.clone().add(new Vector(1, 1, 1)));
    }

    @Override
    public Vector getRandom(Random random) {
        double dx = random.nextDouble();
        double dy = random.nextDouble();
        double dz = random.nextDouble();
        return this.location.clone().add(new Vector(dx, dy, dz));
    }
}
