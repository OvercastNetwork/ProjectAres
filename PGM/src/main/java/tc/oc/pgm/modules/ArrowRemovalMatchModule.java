package tc.oc.pgm.modules;

import org.bukkit.entity.Arrow;
import org.bukkit.event.Listener;
import tc.oc.time.Time;
import tc.oc.pgm.Config;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.Repeatable;

/**
 * Reduce the lifespan of infinity arrows
 */
@ListenerScope(MatchScope.RUNNING)
public class ArrowRemovalMatchModule extends MatchModule implements Listener {

    private final long maxTicks = Config.ArrowRemoval.delay() * 20;

    @Repeatable(interval = @Time(seconds = 1))
    public void repeat() {
        for(Arrow arrow : getMatch().getWorld().getEntitiesByClass(Arrow.class)) {
            if(arrow.getTicksLived() >= this.maxTicks && arrow.getPickupStatus() != Arrow.PickupStatus.ALLOWED) arrow.remove();
        }
    }
}
