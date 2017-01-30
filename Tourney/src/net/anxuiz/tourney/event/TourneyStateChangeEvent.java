package net.anxuiz.tourney.event;

import com.google.common.base.Preconditions;
import net.anxuiz.tourney.TourneyState;
import org.bukkit.event.HandlerList;
import tc.oc.pgm.events.MatchEvent;
import tc.oc.pgm.match.Match;

public class TourneyStateChangeEvent extends MatchEvent {
    private static final HandlerList handlers = new HandlerList();
    private final TourneyState oldState;
    private final TourneyState newState;

    public TourneyStateChangeEvent(Match match, final TourneyState oldState, final TourneyState newState) {
        super(match);
        this.oldState = Preconditions.checkNotNull(oldState, "Old state");
        this.newState = Preconditions.checkNotNull(newState, "New state");
    }

    public static HandlerList getHandlerList() {
        return TourneyStateChangeEvent.handlers;
    }

    public TourneyState getOldState() {
        return oldState;
    }

    public TourneyState getNewState() {
        return newState;
    }

    @Override
    public HandlerList getHandlers() {
        return TourneyStateChangeEvent.handlers;
    }
}
