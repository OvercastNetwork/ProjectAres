package tc.oc.pgm.goals.events;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.event.HandlerList;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.goals.ProximityGoal;

import static com.google.common.base.Preconditions.checkNotNull;

public class GoalProximityChangeEvent extends GoalEvent {
    private final Competitor competitor;
    private final @Nullable Location location;
    private final double oldDistance;
    private final double newDistance;

    public GoalProximityChangeEvent(ProximityGoal goal, Competitor competitor, @Nullable Location location, double oldDistance, double newDistance) {
        super(goal);
        this.competitor = checkNotNull(competitor);
        this.location = location;
        this.oldDistance = oldDistance;
        this.newDistance = newDistance;
    }

    public Competitor getCompetitor() {
        return competitor;
    }

    public @Nullable Location getLocation() {
        return this.location;
    }

    public double getOldDistance() {
        return this.oldDistance;
    }

    public double getNewDistance() {
        return this.newDistance;
    }

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
