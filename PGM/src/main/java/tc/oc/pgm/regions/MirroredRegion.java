package tc.oc.pgm.regions;

import org.bukkit.util.Vector;

import javax.annotation.Nullable;

public class MirroredRegion extends TransformedRegion {
    // Reflection plane equation is: v · normal = offset
    private final @Inspect Vector normal; // unit normal
    private final @Inspect double offset; // parameter of the plane equation

    /**
     * @param region The region that will be mirrored
     * @param origin A point on the reflection plane
     * @param normal The normal of the reflection plane
     */
    public MirroredRegion(Region region, Vector origin, Vector normal) {
        super(region);
        this.normal = new Vector(normal).normalize();
        this.offset = this.normal.dot(origin);
    }

    @Override
    protected Vector transform(Vector point) {
        // FYI, reflection is 2x the projection of the point on the normal
        final Vector reflection = new Vector(normal).multiply(2 * (point.dot(normal) - offset));
        return new Vector(point).subtract(reflection);
    }

    @Override
    protected Vector untransform(Vector point) {
        return this.transform(point);
    }
}
