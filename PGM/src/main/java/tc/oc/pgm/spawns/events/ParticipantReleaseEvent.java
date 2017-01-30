package tc.oc.pgm.spawns.events;

import org.bukkit.event.HandlerList;
import tc.oc.pgm.events.SingleMatchPlayerEvent;
import tc.oc.pgm.match.MatchPlayer;

/**
 * Fires when a player is released from their spawn point, which may be
 * some time after they spawn, due to spawn freezing.
 */
public class ParticipantReleaseEvent extends SingleMatchPlayerEvent {

    private final boolean wasFrozen;

    public ParticipantReleaseEvent(MatchPlayer player, boolean wasFrozen) {
        super(player);
        this.wasFrozen = wasFrozen;
    }

    public boolean wasFrozen() {
        return wasFrozen;
    }

    private static final HandlerList handlers = new HandlerList();
}
