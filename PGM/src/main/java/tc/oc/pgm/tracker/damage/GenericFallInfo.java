package tc.oc.pgm.tracker.damage;

import javax.annotation.Nullable;

import org.bukkit.Location;
import tc.oc.commons.core.inspect.Inspectable;
import tc.oc.pgm.match.ParticipantState;

import static com.google.common.base.Preconditions.checkNotNull;

public class GenericFallInfo extends Inspectable.Impl implements FallInfo {

    @Inspect private final To to;
    @Inspect private final Location origin;

    public GenericFallInfo(To to, Location origin) {
        this.to = checkNotNull(to);
        this.origin = checkNotNull(origin);
    }

    public GenericFallInfo(To to, Location location, double distance) {
        this(to, location.clone().add(0, distance, 0));
    }

    @Override
    public From getFrom() {
        return From.GROUND;
    }

    @Override
    public To getTo() {
        return to;
    }

    @Override
    public @Nullable TrackerInfo getCause() {
        return null;
    }

    @Override
    public @Nullable ParticipantState getAttacker() {
        return null;
    }

    @Override
    public Location getOrigin() {
        return origin;
    }
}
