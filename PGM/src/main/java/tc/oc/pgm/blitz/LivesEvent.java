package tc.oc.pgm.blitz;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when the amount of {@link Lives} changes.
 */
public class LivesEvent extends Event {

    private final Lives lives;

    public LivesEvent(Lives lives) {
        this.lives = lives;
    }

    public Lives lives() {
        return lives;
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
