package tc.oc.pgm.tracker.damage;

import org.bukkit.Location;
import tc.oc.pgm.match.ParticipantState;
import static com.google.common.base.Preconditions.checkNotNull;

public class TNTInfo extends OwnerInfoBase implements RangedInfo {

    @Inspect private final Location origin;

    public TNTInfo(ParticipantState owner, Location origin) {
        super(owner);
        this.origin = checkNotNull(origin);
    }

    @Override
    public Location getOrigin() {
        return origin;
    }
}
