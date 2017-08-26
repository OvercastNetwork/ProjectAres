package tc.oc.pgm.blitz;

import org.bukkit.event.HandlerList;
import tc.oc.pgm.events.MatchEvent;
import tc.oc.pgm.match.Match;

/**
 * Called when {@link BlitzMatchModule} is enabled or disabled, even during a match.
 */
public class BlitzEvent extends MatchEvent {

    private final BlitzMatchModule blitz;

    public BlitzEvent(Match match, BlitzMatchModule blitz) {
        super(match);
        this.blitz = blitz;
    }

    public BlitzMatchModule blitz() {
        return blitz;
    }

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
