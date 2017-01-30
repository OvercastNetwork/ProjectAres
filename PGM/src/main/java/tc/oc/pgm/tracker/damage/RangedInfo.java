package tc.oc.pgm.tracker.damage;

import javax.annotation.Nullable;

import org.bukkit.Location;

public interface RangedInfo extends TrackerInfo {

    default double distanceFrom(@Nullable Location deathLocation) {
        if(getOrigin() == null || deathLocation == null) return Double.NaN;

        // When players fall in the void, use y=0 as their death location
        if(deathLocation.getY() < 0) {
            deathLocation = deathLocation.clone();
            deathLocation.setY(0);
        }
        return deathLocation.distance(getOrigin());
    }

    @Nullable Location getOrigin();
}
