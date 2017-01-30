package net.anxuiz.tourney.event.mapselect;

import com.google.common.base.Preconditions;
import net.anxuiz.tourney.vote.VetoVote;
import org.bukkit.event.HandlerList;
import tc.oc.pgm.map.PGMMap;

public class MapSelectionMapSelectEvent extends MapSelectionEvent {
    private static final HandlerList handlers = new HandlerList();
    private final PGMMap map;

    public MapSelectionMapSelectEvent(final VetoVote vote, final PGMMap map) {
        super(vote);
        this.map = Preconditions.checkNotNull(map, "Map");
    }

    public static HandlerList getHandlerList() {
        return MapSelectionMapSelectEvent.handlers;
    }

    public PGMMap getMap() {
        return this.map;
    }

    @Override
    public HandlerList getHandlers() {
        return MapSelectionMapSelectEvent.handlers;
    }
}
