package net.anxuiz.tourney.event.mapselect;

import com.google.common.base.Preconditions;
import net.anxuiz.tourney.MapClassification;
import net.anxuiz.tourney.vote.VetoVote;
import org.bukkit.event.HandlerList;

public class MapSelectionClassificationSelectEvent extends MapSelectionEvent {
    private static final HandlerList handlers = new HandlerList();
    private final MapClassification classification;

    public MapSelectionClassificationSelectEvent(final VetoVote vote, final MapClassification classification) {
        super(vote);
        this.classification = Preconditions.checkNotNull(classification, "Classification");
    }

    public static HandlerList getHandlerList() {
        return MapSelectionClassificationSelectEvent.handlers;
    }

    public MapClassification getClassification() {
        return this.classification;
    }

    @Override
    public HandlerList getHandlers() {
        return MapSelectionClassificationSelectEvent.handlers;
    }
}
