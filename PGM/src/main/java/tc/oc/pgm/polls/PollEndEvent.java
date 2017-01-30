package tc.oc.pgm.polls;

import org.bukkit.event.HandlerList;

/**
 * Called when a poll ends.
 */
public class PollEndEvent extends PollEvent {
    private static final HandlerList handlers = new HandlerList();

    private final PollEndReason reason;

    public PollEndEvent(Poll poll, PollEndReason reason) {
        super(poll);
        this.reason = reason;
    }

    public PollEndReason getReason() {
        return this.reason;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
