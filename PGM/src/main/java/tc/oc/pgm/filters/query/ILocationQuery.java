package tc.oc.pgm.filters.query;

import org.bukkit.Location;
import org.bukkit.util.ImVector;

public interface ILocationQuery extends IMatchQuery {

    Location getLocation();

    default ImVector blockCenter() {
        return ImVector.copyOf(getLocation().position().blockCenter());
    }

    @Override
    default int randomSeed() {
        return getLocation().hashCode();
    }
}
