package tc.oc.pgm.regions;

import org.bukkit.geometry.Cuboid;
import org.bukkit.util.ImVector;
import org.bukkit.util.Vector;

public class SectorRegion extends Region.Impl {
    protected final @Inspect ImVector center;
    protected final @Inspect double startAngle;
    protected final @Inspect double endAngle;

    public SectorRegion(double x, double z, double startAngle, double endAngle) {
        this.center = ImVector.of(x, 0, z);
        this.startAngle = startAngle;
        this.endAngle = endAngle;
    }

    @Override
    public boolean contains(Vector point) {
        double dx = point.getX() - center.getX();
        double dz = point.getZ() - center.getZ();
        if(dx == 0 && dz == 0) {
            return true;
        }

        double atan2 = Math.atan2(dz, dx);
        if(atan2 < 0) atan2 += 2 * Math.PI;
        return this.startAngle <= atan2 && atan2 <= this.endAngle;
    }

    @Override
    public Cuboid getBounds() {
        return Cuboid.unbounded();
    }
}
