package tc.oc.pgm.destroyable;

import java.util.UUID;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.event.HandlerList;
import tc.oc.pgm.events.MatchUserEvent;
import tc.oc.pgm.match.Match;

/**
 * Called when an a {@link Destroyable} is damaged or repaired.
 *
 * Event is fired before the info is added to the destroyable's list.
 *
 * @see DestroyableHealthChange
 */
public class DestroyableHealthChangeEvent extends DestroyableEvent implements MatchUserEvent {
    public DestroyableHealthChangeEvent(@Nonnull Match match,
                                        @Nonnull Destroyable destroyable,
                                        @Nullable DestroyableHealthChange change) {
        super(match, destroyable);
        this.change = change;
    }

    /**
     * Gets the information associated with this event. This may be null in cases
     * where there are no details available about the event. In this case, anything
     * about the Destroyable could have changed.
     *
     * @return Event information
     */
    public @Nullable DestroyableHealthChange getChange() {
        return this.change;
    }

    private final @Nullable DestroyableHealthChange change;

    @Override
    public Stream<UUID> users() {
        return change != null && change.getPlayerCause() != null
               ? Stream.of(change.getPlayerCause().getUniqueId())
               : Stream.empty();
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
