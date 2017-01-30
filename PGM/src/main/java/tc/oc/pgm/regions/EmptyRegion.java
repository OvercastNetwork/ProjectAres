package tc.oc.pgm.regions;

import org.bukkit.geometry.Cuboid;
import org.bukkit.util.Vector;

public class EmptyRegion extends Region.Impl {
    public static final EmptyRegion INSTANCE = new EmptyRegion();

    @Override
    public boolean contains(Vector point) {
        return false;
    }

    @Override
    public boolean isBlockBounded() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public Cuboid getBounds() {
        return Cuboid.empty();
    }
}
