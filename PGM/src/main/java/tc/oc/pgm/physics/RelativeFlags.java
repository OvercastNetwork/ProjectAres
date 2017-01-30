package tc.oc.pgm.physics;

import org.bukkit.Location;
import tc.oc.commons.bukkit.geometry.AffineTransform;
import tc.oc.commons.core.util.Utils;

public class RelativeFlags {

    private static final RelativeFlags
        FF = new RelativeFlags(false, false),
        FT = new RelativeFlags(false, true),
        TF = new RelativeFlags(true, false),
        TT = new RelativeFlags(true, true);

    public static RelativeFlags of(boolean relativeYaw, boolean relativePitch) {
        return relativeYaw ? relativePitch ? TT
                                           : TF
                           : relativePitch ? FT
                                           : FF;
    }

    private final boolean relativeYaw, relativePitch;

    private RelativeFlags(boolean relativeYaw, boolean relativePitch) {
        this.relativeYaw = relativeYaw;
        this.relativePitch = relativePitch;
    }

    public boolean relativeYaw() {
        return relativeYaw;
    }

    public boolean relativePitch() {
        return relativePitch;
    }

    public AffineTransform getTransform(Location location) {
        return getTransform(location.getYaw(), location.getPitch());
    }

    public AffineTransform getTransform(double yaw, double pitch) {
        AffineTransform transform = AffineTransform.identity();
        if(relativeYaw) {
            transform = transform.append(AffineTransform.forYaw(yaw));
        }
        if(relativePitch) {
            transform = transform.append(AffineTransform.forPitch(pitch));
        }
        return transform;
    }

    @Override
    public boolean equals(Object obj) {
        return Utils.equals(RelativeFlags.class, this, obj, that ->
            this.relativeYaw == that.relativeYaw &&
            this.relativePitch == that.relativePitch
        );
    }

    @Override
    public int hashCode() {
        return (relativeYaw ? 2 : 0) + (relativePitch ? 1 : 0);
    }
}
