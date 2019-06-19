package tc.oc.pgm.effect;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import tc.oc.pgm.Config;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.utils.EntityUtils;

/**
 * Render a particle explosion when a player's TNT explodes from a far distance.
 */
@ListenerScope(MatchScope.RUNNING)
public class LongRangeExplosionMatchModule extends MatchModule implements Listener {

    private static final double MIN_DISTANCE_SQ = 60 * 60; // Minimum square distance to show fake explosions

    private void render(Location location) {
        EntityUtils.entities(match.getWorld(), Player.class)
             .filter(viewer -> viewer.getLocation().distanceSquared(location) >= MIN_DISTANCE_SQ)
             .forEach(viewer -> viewer.playEffect(location, Effect.EXPLOSION_LARGE, null));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void entityExplode(EntityExplodeEvent event) {
        render(event.getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void blockExplode(BlockExplodeEvent event) {
        render(event.getBlock().getLocation());
    }

    @Override
    public boolean shouldLoad() {
        return Config.Effects.explosions();
    }
}
