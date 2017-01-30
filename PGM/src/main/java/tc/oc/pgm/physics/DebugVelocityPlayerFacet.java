package tc.oc.pgm.physics;

import java.util.logging.Logger;
import javax.inject.Inject;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import tc.oc.commons.bukkit.util.Vectors;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.pgm.match.MatchPlayerFacet;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.Repeatable;

public class DebugVelocityPlayerFacet implements MatchPlayerFacet {

    private final Logger logger;
    private final Player player;
    private boolean enabled;

    @Inject DebugVelocityPlayerFacet(Loggers loggers, Player player) {
        this.logger = loggers.get(getClass(), player.getName());
        this.player = player;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private static String format(Vector v) {
        return Vectors.format(v, "%.3f");
    }

    @Repeatable(scope = MatchScope.LOADED)
    public void tick() {
        if(enabled) {
            logger.info(
                "predicted=" + format(player.getPredictedVelocity()) +
                " client=" + format(player.getClientVelocity()) +
                " impulse=" + format(player.getUnackedImpulse()) +
                " unacked=" + player.hasUnackedVelocity()
            );
        }
    }
}
