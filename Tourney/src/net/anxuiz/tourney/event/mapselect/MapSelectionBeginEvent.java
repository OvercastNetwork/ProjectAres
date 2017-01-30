package net.anxuiz.tourney.event.mapselect;

import com.google.common.base.Preconditions;
import net.anxuiz.tourney.vote.VetoVote;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MapSelectionBeginEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final VetoVote vote;

    public MapSelectionBeginEvent(final VetoVote vote) {
        this.vote = Preconditions.checkNotNull(vote, "Vote");
    }

    public static HandlerList getHandlerList() {
        return MapSelectionBeginEvent.handlers;
    }

    public VetoVote getVote() {
        return this.vote;
    }

    @Override
    public HandlerList getHandlers() {
        return MapSelectionBeginEvent.handlers;
    }
}
