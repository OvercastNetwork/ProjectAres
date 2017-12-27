package tc.oc.pgm.payload.events;

import org.bukkit.event.HandlerList;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.payload.Payload;

import javax.annotation.Nullable;

public class ControllerChangeEvent extends PayloadPointEvent {
    private static final HandlerList handlers = new HandlerList();
    @Nullable private final Competitor oldController;
    @Nullable private final Competitor newController;

    public ControllerChangeEvent(Match match, Payload payload, Competitor oldController, Competitor newController) {
        super(match, payload);
        this.oldController = oldController;
        this.newController = newController;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Nullable
    public Competitor getNewController() {
        return newController;
    }

    @Nullable
    public Competitor getOldController() {
        return oldController;
    }
}
