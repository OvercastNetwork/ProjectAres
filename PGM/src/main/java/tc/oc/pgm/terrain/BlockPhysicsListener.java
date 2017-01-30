package tc.oc.pgm.terrain;

import javax.inject.Inject;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchScope;

/**
 * Freeze block physics after the match ends, and before it starts too,
 * unless pre-match-physics is enabled.
 */
@ListenerScope(MatchScope.LOADED)
public class BlockPhysicsListener implements Listener {

    private final TerrainOptions options;
    private final Match match;

    @Inject private BlockPhysicsListener(TerrainOptions options, Match match) {
        this.options = options;
        this.match = match;
    }

    private boolean allowPhysics() {
        if(match.isFinished()) return false;
        if(match.hasStarted()) return true;
        return options.initialPhysics();
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if(!allowPhysics()) {
            event.setCancelled(true);
        }
    }
}
