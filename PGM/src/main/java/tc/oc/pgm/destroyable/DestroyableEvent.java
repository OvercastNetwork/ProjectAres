package tc.oc.pgm.destroyable;

import javax.annotation.Nonnull;

import tc.oc.pgm.match.Match;
import tc.oc.pgm.events.MatchEvent;

import com.google.common.base.Preconditions;

/**
 * Abstract superclass for {@link Destroyable} related events.
 */
public abstract class DestroyableEvent extends MatchEvent {
    private final @Nonnull Destroyable destroyable;

    public DestroyableEvent(@Nonnull Match match, @Nonnull Destroyable destroyable) {
        super(match);

        Preconditions.checkNotNull(destroyable, "destroyable");

        this.destroyable = destroyable;
    }

    /**
     * Gets the destroyable involved in this event.
     *
     * @return Destroyable involved
     */
    public @Nonnull Destroyable getDestroyable() {
        return this.destroyable;
    }
}
