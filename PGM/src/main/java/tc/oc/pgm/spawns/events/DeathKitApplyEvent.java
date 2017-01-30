package tc.oc.pgm.spawns.events;

import org.bukkit.event.HandlerList;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.events.SingleMatchPlayerEvent;

/**
 * Called shortly after a participating player dies and the time is right
 * to give them any items that they can use while on the death screen.
 */
public class DeathKitApplyEvent extends SingleMatchPlayerEvent {

    public DeathKitApplyEvent(MatchPlayer player) {
        super(player);
    }

    private static final HandlerList handlers = new HandlerList();
    @Override public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
