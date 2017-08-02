package tc.oc.pgm.regions;

import org.bukkit.geometry.Cuboid;
import org.bukkit.util.Vector;

public class HalfspaceRegion extends Region.Impl {
    private final @Inspect Vector normal; // unit normal
    private final @Inspect double offset; // parameter of the plane equation

    public HalfspaceRegion(Vector origin, Vector normal) {
        this.normal = new Vector(normal).normalize();
        this.offset = this.normal.dot(origin);
    }

    @Override
    public boolean contains(Vector point) {
        return this.normal.dot(point) >= offset;
    }

    @Override
    public Cuboid getBounds() {
        return Cuboid.unbounded();
    }
}
