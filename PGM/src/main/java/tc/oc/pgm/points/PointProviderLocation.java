package tc.oc.pgm.points;

import java.util.OptionalDouble;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.geometry.Vec3;

/**
 * The only purpose of this class is to allow callers of {@link PointProvider#getPoint} to
 * detect if the provider set an angle or not, which the CTF module needs to do.
 *
 * The other potential solutions were even more hacky.
 */
public class PointProviderLocation extends Location {

    private OptionalDouble yaw;
    private OptionalDouble pitch;

    public PointProviderLocation(World world, Vec3 position) {
        super(world, position);
        this.yaw = this.pitch = OptionalDouble.empty();
    }

    public PointProviderLocation(World world, Vec3 position, OptionalDouble yaw, OptionalDouble pitch) {
        super(world, position, (float) yaw.orElse(0), (float) pitch.orElse(0));
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public boolean hasYaw() {
        return yaw.isPresent();
    }

    public boolean hasPitch() {
        return pitch.isPresent();
    }

    void clearYaw() {
        this.yaw = OptionalDouble.empty();
    }

    void clearPitch() {
        this.pitch = OptionalDouble.empty();
    }

    @Override
    public void setYaw(float yaw) {
        super.setYaw(yaw);
        this.yaw = OptionalDouble.of(yaw);
    }

    @Override
    public void setPitch(float pitch) {
        super.setPitch(pitch);
        this.pitch = OptionalDouble.of(pitch);
    }

    @Override
    public PointProviderLocation clone() {
        return new PointProviderLocation(getWorld(), position().copy(), yaw, pitch);
    }
}
