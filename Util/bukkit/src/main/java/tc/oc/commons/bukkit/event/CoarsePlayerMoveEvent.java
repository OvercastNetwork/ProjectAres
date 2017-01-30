package tc.oc.commons.bukkit.event;

import org.bukkit.EntityLocation;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerMoveEvent;
import tc.oc.commons.bukkit.util.BlockUtils;

/**
 * Wraps PlayerMoveEvents that cross block boundaries. The from and to locations
 * are the same as the wrapped event, so the locations for consecutive coarse events
 * will not generally connect to each other.
 *
 * Cancelling a coarse event results in the player's position being reset to the
 * center of the block at the from location, with some adjustments to Y to try and
 * place them on the surface of the block.
 */
public class CoarsePlayerMoveEvent extends GeneralizingEvent {
    private static final HandlerList handlers = new HandlerList();

    protected final Player player;
    protected final EntityLocation from;
    protected EntityLocation to;

    public CoarsePlayerMoveEvent(Event cause, Player player, EntityLocation from, EntityLocation to) {
        super(cause);
        this.player = player;
        this.from = from;
        this.to = to;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
               "{from=" + from +
               " to=" + to +
               " player=" + player +
               " cause=" + (getCause() == null ? null : getCause().getClass().getSimpleName()) +
               "}";
    }

    public Player getPlayer() {
        return this.player;
    }

    @Override
    public Player getActor() {
        return getPlayer();
    }

    public EntityLocation getFrom() {
        return this.from;
    }

    public EntityLocation getBlockFrom() {
        return BlockUtils.center(this.from);
    }

    public EntityLocation getTo() {
        return this.to;
    }

    public EntityLocation getBlockTo() {
        return BlockUtils.center(this.to);
    }

    public void setTo(EntityLocation newLoc) {
        if(this.cause instanceof PlayerMoveEvent) {
            ((PlayerMoveEvent) this.cause).setTo(newLoc);
        }
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
