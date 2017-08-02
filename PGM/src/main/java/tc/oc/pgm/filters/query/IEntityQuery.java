package tc.oc.pgm.filters.query;

import java.util.Objects;
import java.util.Set;

import org.bukkit.EntityLocation;
import org.bukkit.Location;
import org.bukkit.PoseFlag;

public interface IEntityQuery extends IEntityTypeQuery, ILocationQuery, IPoseQuery {

    EntityLocation getEntityLocation();

    @Override
    default Location getLocation() {
        return getEntityLocation();
    }

    @Override
    default Set<PoseFlag> getPose() {
        return getEntityLocation().poseFlags();
    }

    @Override
    default int randomSeed() {
        return Objects.hash(getEntityType(), getEntityLocation(), getPose());
    }
}
