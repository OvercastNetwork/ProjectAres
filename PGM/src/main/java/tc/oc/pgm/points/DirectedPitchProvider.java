package tc.oc.pgm.points;

import static com.google.common.base.Preconditions.checkNotNull;

import org.bukkit.util.Vector;
import tc.oc.commons.core.inspect.Inspectable;

public class DirectedPitchProvider extends Inspectable.Impl implements AngleProvider {

    private final @Inspect Vector target;

    public DirectedPitchProvider(Vector target) {
        this.target = checkNotNull(target, "target");
    }

    @Override
    public float getAngle(Vector from) {
        double dx = this.target.getX() - from.getX();
        double dz = this.target.getZ() - from.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        double dy = this.target.getY() - (from.getY() + 1.62); // add eye height so player actually looks at point
        return (float) Math.toDegrees(Math.atan2(-dy, distance));
    }

    @Override
    public boolean isConstant() {
        return true;
    }
}
