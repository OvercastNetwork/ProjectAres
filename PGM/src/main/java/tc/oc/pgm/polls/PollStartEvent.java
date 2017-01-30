package tc.oc.pgm.polls;

import org.bukkit.event.HandlerList;

/**
 * Called when a poll first starts.
 */
public class PollStartEvent extends PollEvent {
    private static final HandlerList handlers = new HandlerList();

    public PollStartEvent(Poll poll) {
        super(poll);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
