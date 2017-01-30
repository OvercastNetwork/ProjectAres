package tc.oc.pgm.filters.matcher.entity;

import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import tc.oc.pgm.filters.matcher.TypedFilter;
import tc.oc.pgm.filters.query.IEntitySpawnQuery;

public class SpawnReasonFilter extends TypedFilter.Impl<IEntitySpawnQuery> {
    protected final @Inspect SpawnReason reason;

    public SpawnReasonFilter(SpawnReason reason) {
        this.reason = reason;
    }

    @Override
    public boolean matches(IEntitySpawnQuery query) {
        return reason == query.getSpawnReason();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{reason=" + this.reason + "}";
    }
}
