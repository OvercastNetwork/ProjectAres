package tc.oc.pgm.points;

import static com.google.common.base.Preconditions.checkNotNull;

import org.bukkit.util.Vector;
import tc.oc.commons.core.inspect.Inspectable;

public class DirectedYawProvider extends Inspectable.Impl implements AngleProvider {

    private final @Inspect Vector target;

    public DirectedYawProvider(Vector target) {
        this.target = checkNotNull(target, "target");
    }

    @Override
    public float getAngle(Vector from) {
        double dx = this.target.getX() - from.getX();
        double dz = this.target.getZ() - from.getZ();
        return (float) Math.toDegrees(Math.atan2(-dx, dz));
    }

    @Override
    public boolean isConstant() {
        return true;
    }
}
