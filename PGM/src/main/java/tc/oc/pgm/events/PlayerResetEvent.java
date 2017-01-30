package tc.oc.pgm.events;

import org.bukkit.event.HandlerList;
import tc.oc.pgm.match.MatchPlayer;

/**
 * Called immediately after a MatchPlayer is "reset" i.e. set to the default state
 */
public class PlayerResetEvent extends SingleMatchPlayerEvent {

    public PlayerResetEvent(MatchPlayer player) {
        super(player);
    }

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
