package tc.oc.pgm.events;

import org.bukkit.event.HandlerList;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.MatchPlayer;

public abstract class PlayerParticipationEvent extends SingleMatchPlayerEvent {

    private final Competitor competitor;

    protected PlayerParticipationEvent(MatchPlayer player, Competitor competitor) {
        super(player);
        this.competitor = competitor;
    }

    /**
     * NOTE: this Competitor MAY not be in the match at this point
     */
    public Competitor getCompetitor() {
        return competitor;
    }

    private static final HandlerList handlers = new HandlerList();
    @Override public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
