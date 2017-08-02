package tc.oc.pgm.regions;

import org.bukkit.geometry.Cuboid;
import org.bukkit.util.Vector;

/**
 * Wherever you go, here you are
 */
public class EverywhereRegion extends Region.Impl {
    public static final EverywhereRegion INSTANCE = new EverywhereRegion();

    @Override
    public boolean contains(Vector point) {
        return true;
    }

    @Override
    public Cuboid getBounds() {
        return Cuboid.unbounded();
    }

    @Override
    public boolean isEverywhere() {
        return true;
    }
}
