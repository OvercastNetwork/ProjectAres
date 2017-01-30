package tc.oc.pgm.time;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.bukkit.World;
import java.time.Instant;
import tc.oc.commons.bukkit.util.NMSHacks;
import tc.oc.time.Clock;

/**
 * Quantizes time to the ticks of the given World. Guaranteed to return the same
 * time over the duration of any tick.
 */
public class WorldTickClock implements Clock, TickClock {

    private final World world;
    private @Nullable TickTime now;

    @Inject public WorldTickClock(World world) {
        this.world = world;
    }

    @Override
    public Instant instant() {
        return now().instant;
    }

    @Override
    public TickTime now() {
        long tick = NMSHacks.getMonotonicTime(this.world);
        if(this.now == null || tick != this.now.tick) {
            this.now = new TickTime(tick, Instant.now());
        }
        return this.now;
    }
}
