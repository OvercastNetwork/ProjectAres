package tc.oc.pgm.events;

import org.bukkit.event.Cancellable;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.MatchPlayer;

/**
 * Called immediately before a player leaves a {@link Competitor}.
 * This differs from {@link PlayerPartyChangeEvent} in a few ways:
 *
 *  - It is only called when leaving a party, and only when that party is participating
 *  - It is called before the change
 */
public class PlayerParticipationStopEvent extends PlayerParticipationEvent implements Cancellable {

    private boolean cancelled;

    public PlayerParticipationStopEvent(MatchPlayer player, Competitor competitor) {
        super(player, competitor);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
