package tc.oc.pgm.regions;

import java.util.stream.Stream;

import org.bukkit.geometry.Cuboid;
import org.bukkit.util.Vector;

public class NegativeRegion extends Region.Impl {
    protected final @Inspect Region region;

    public NegativeRegion(Region region) {
        this.region = region;
    }

    public static Region of(Region region) {
        if(region instanceof EverywhereRegion) {
            return EmptyRegion.INSTANCE;
        } else if(region instanceof EmptyRegion) {
            return EverywhereRegion.INSTANCE;
        } else if(region instanceof NegativeRegion) {
            return ((NegativeRegion) region).region;
        } else {
            return new NegativeRegion(region);
        }
    }

    @Override
    public Stream<? extends Region> dependencies() {
        return Stream.of(region);
    }

    @Override
    public boolean contains(Vector point) {
        return !region.contains(point);
    }

    @Override
    public boolean isBlockBounded() {
        return false;
    }

    @Override
    public Cuboid getBounds() {
        throw new UnsupportedOperationException("NegativeRegion is unbounded");
    }

    @Override
    public String inspectType() {
        return "Negative";
    }
}
