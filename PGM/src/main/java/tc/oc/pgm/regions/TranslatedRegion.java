package tc.oc.pgm.regions;

import org.bukkit.geometry.Cuboid;
import org.bukkit.util.Vector;

/**
 * Region adaptor that applies a translation.
 */
public class TranslatedRegion extends TransformedRegion {
    private final @Inspect Vector offset;

    public TranslatedRegion(Region region, Vector offset) {
        super(region);
        this.offset = offset;
    }

    public static TranslatedRegion translate(Region region, Vector offset) {
        return new TranslatedRegion(region, offset);
    }

    @Override
    protected Vector transform(Vector point) {
        return new Vector(point).add(this.offset);
    }

    @Override
    protected Vector untransform(Vector point) {
        return new Vector(point).subtract(this.offset);
    }

    @Override
    protected Cuboid getTransformedBounds() {
        return this.region.getBounds().translate(this.offset);
    }
}
