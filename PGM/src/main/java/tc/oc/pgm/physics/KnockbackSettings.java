package tc.oc.pgm.physics;

import org.bukkit.util.Vector;
import tc.oc.commons.bukkit.util.Vectors;

public class KnockbackSettings {

    public static final KnockbackSettings DEFAULT = new KnockbackSettings(0, 0, 0, Double.POSITIVE_INFINITY, 0, 0);

    public final double pitch;
    public final double walkPower;
    public final double sprintPower;
    public final double sprintThreshold;
    public final double recoilGround;
    public final double recoilAir;

    public Vector pitchedNormal(Vector delta) {
        delta = delta.clone();
        delta.setY(0);
        if(delta.isZero()) return Vectors.ZERO;
        delta.normalize();
        final double theta = Math.toRadians(pitch);
        final double cos = Math.cos(theta);
        delta.set(cos * delta.getX(),
                   Math.sin(theta),
                   cos * delta.getZ());
        return delta;
    }

    public double power(boolean sprint) {
        return sprint ? sprintPower : walkPower;
    }

    public double recoil(boolean onGround) {
        return onGround ? recoilGround : recoilAir;
    }

    public KnockbackSettings(double pitch, double walkPower, double sprintPower, double sprintThreshold, double recoilGround, double recoilAir) {
        this.pitch = pitch;
        this.walkPower = walkPower;
        this.sprintPower = sprintPower;
        this.sprintThreshold = sprintThreshold;
        this.recoilGround = recoilGround;
        this.recoilAir = recoilAir;
    }
}
