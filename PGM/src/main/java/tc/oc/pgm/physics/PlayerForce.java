package tc.oc.pgm.physics;

import org.bukkit.util.ImVector;
import org.bukkit.util.Vector;
import tc.oc.pgm.match.MatchPlayer;

public class PlayerForce {

    private final ImVector acceleration;
    private final RelativeFlags relative;

    public PlayerForce(Vector acceleration, RelativeFlags relative) {
        this.acceleration = ImVector.copyOf(acceleration);
        this.relative = relative;
    }

    public Vector acceleration(MatchPlayer player) {
        return relative.getTransform(player.getLocation()).apply(acceleration);
    }
}
