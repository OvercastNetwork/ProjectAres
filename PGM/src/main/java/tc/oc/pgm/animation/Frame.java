package tc.oc.pgm.animation;

import org.bukkit.World;
import org.bukkit.util.Vector;
import tc.oc.pgm.features.Feature;

/**
 * Created from a {@link FrameDefinition} for a specific {@link World}.
 */
public interface Frame extends Feature<FrameDefinition> {

    /**
     * Place this frame in its origin world, offset by the given delta.
     */
    void place(World world, Vector newLocation);

    void setOrigin(Vector origin);

    Vector getOrigin();
}
