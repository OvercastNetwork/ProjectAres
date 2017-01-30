package net.anxuiz.tourney.event.mapselect;

import net.anxuiz.tourney.vote.VetoVote;
import org.bukkit.event.HandlerList;

public class MapSelectionTurnCycleEvent extends MapSelectionEvent {
    private static final HandlerList handlers = new HandlerList();

    public MapSelectionTurnCycleEvent(final VetoVote vote) {
        super(vote);
    }

    public static HandlerList getHandlerList() {
        return MapSelectionTurnCycleEvent.handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return MapSelectionTurnCycleEvent.handlers;
    }
}
