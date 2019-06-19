package tc.oc.pgm.payload.events;

import org.bukkit.event.HandlerList;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.payload.Payload;

public class CapturingTimeChangeEvent extends PayloadPointEvent {
    private static final HandlerList handlers = new HandlerList();

    public CapturingTimeChangeEvent(Match match, Payload point) {
        super(match, point);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
