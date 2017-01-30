package tc.oc.pgm.filters.query;

import java.util.Objects;

import org.bukkit.event.entity.CreatureSpawnEvent;

public interface IEntitySpawnQuery extends IEntityTypeQuery, IEventQuery {

    CreatureSpawnEvent.SpawnReason getSpawnReason();

    @Override
    default int randomSeed() {
        return Objects.hash(getEvent(), getEntityType(), getSpawnReason());
    }
}
