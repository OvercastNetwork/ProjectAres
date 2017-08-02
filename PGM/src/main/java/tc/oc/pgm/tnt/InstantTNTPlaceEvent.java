package tc.oc.pgm.tnt;

import javax.annotation.Nullable;

import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ExplosionPrimeByEntityEvent;
import tc.oc.pgm.listeners.BlockTransformListener;

/**
 * Special case of {@link ExplosionPrimeByEntityEvent} for instant-ignite TNT.
 *
 * This allows {@link BlockTransformListener} to NOT treat the event as a block break,
 * even though it can see a TNT block at the location. The block is there because
 * cancelling the {@link BlockPlaceEvent} doesn't remove it until after the event returns.
 *
 * TODO: A better solution would be to make sure the TNT block is gone before
 * calling the {@link ExplosionPrimeByEntityEvent}, but I can't think of a nice
 * way to do that for now.
 */
public class InstantTNTPlaceEvent extends ExplosionPrimeByEntityEvent {
    public InstantTNTPlaceEvent(TNTPrimed tnt, @Nullable Entity placer) {
        super(tnt, placer);
    }
}
