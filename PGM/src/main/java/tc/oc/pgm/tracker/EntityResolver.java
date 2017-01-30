package tc.oc.pgm.tracker;

import javax.annotation.Nullable;

import org.bukkit.entity.Entity;
import tc.oc.pgm.match.ParticipantState;
import tc.oc.pgm.tracker.damage.PhysicalInfo;
import tc.oc.pgm.tracker.damage.TrackerInfo;

public interface EntityResolver {

    PhysicalInfo resolveEntity(Entity entity);

    @Nullable TrackerInfo resolveInfo(Entity entity);

    default @Nullable <T extends TrackerInfo> T resolveInfo(Entity entity, Class<T> infoType) {
        TrackerInfo info = resolveInfo(entity);
        return infoType.isInstance(info) ? infoType.cast(info) : null;
    }

    @Nullable ParticipantState getOwner(Entity entity);
}
