package tc.oc.pgm.filters.query;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.CreatureSpawnEvent;

import static com.google.common.base.Preconditions.checkNotNull;

public class EntitySpawnQuery extends EntityQuery implements IEntitySpawnQuery {

    private final Event event;
    private final CreatureSpawnEvent.SpawnReason spawnReason;

    public EntitySpawnQuery(Event event, Entity entity, CreatureSpawnEvent.SpawnReason spawnReason) {
        super(entity);
        this.event = checkNotNull(event);
        this.spawnReason = checkNotNull(spawnReason);
    }

    @Override
    public Event getEvent() {
        return event;
    }

    @Override
    public CreatureSpawnEvent.SpawnReason getSpawnReason() {
        return spawnReason;
    }

    @Override
    public int randomSeed() {
        return IEntitySpawnQuery.super.randomSeed();
    }
}
