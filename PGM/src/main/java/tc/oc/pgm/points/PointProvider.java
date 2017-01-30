package tc.oc.pgm.points;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.regions.Region;

public interface PointProvider {

    @Nullable Location getPoint(Match match, @Nullable Entity entity);

    int getWeight();

    Region getRegion();

    boolean canFail();
}
