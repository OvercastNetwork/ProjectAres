package tc.oc.pgm.kits;

import org.bukkit.util.ImVector;
import org.bukkit.util.Vector;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.physics.RelativeFlags;

public class ImpulseKit extends Kit.Impl {

    private final ImVector velocity;
    private final RelativeFlags relative;

    public ImpulseKit(Vector velocity, RelativeFlags relative) {
        this.velocity = ImVector.copyOf(velocity);
        this.relative = relative;
    }

    @Override
    public void apply(MatchPlayer player, boolean force, ItemKitApplicator items) {
        player.getBukkit().setVelocity(relative.getTransform(player.getLocation()).apply(velocity));
    }
}
