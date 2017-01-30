package tc.oc.pgm.points;

import org.bukkit.util.Vector;
import tc.oc.commons.core.inspect.Inspectable;

public class StaticAngleProvider extends Inspectable.Impl implements AngleProvider {

    private final @Inspect float degrees;

    public StaticAngleProvider(float degrees) {
        this.degrees = degrees;
    }

    @Override
    public float getAngle(Vector from) {
        return this.degrees;
    }

    @Override
    public boolean isConstant() {
        return true;
    }
}
