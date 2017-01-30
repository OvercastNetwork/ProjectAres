package tc.oc.pgm.filters.query;

import org.bukkit.EntityLocation;
import org.bukkit.entity.Entity;
import tc.oc.pgm.PGM;
import tc.oc.pgm.match.Match;

import static com.google.common.base.Preconditions.checkNotNull;

public class EntityQuery implements IEntityQuery {

    private final Entity entity;
    private final Match match;

    public EntityQuery(Entity entity) {
        this.entity = checkNotNull(entity);
        this.match = PGM.getMatchManager().getMatch(entity.getWorld());
    }

    @Override
    public EntityLocation getEntityLocation() {
        return entity.getEntityLocation();
    }

    @Override
    public Class<? extends Entity> getEntityType() {
        return entity.getClass();
    }

    @Override
    public Match getMatch() {
        return match;
    }
}
