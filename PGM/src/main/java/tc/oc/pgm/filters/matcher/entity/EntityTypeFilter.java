package tc.oc.pgm.filters.matcher.entity;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import tc.oc.pgm.filters.matcher.TypedFilter;
import tc.oc.pgm.filters.query.IEntityTypeQuery;

public class EntityTypeFilter extends TypedFilter.Impl<IEntityTypeQuery> {
    private final @Inspect Class<? extends Entity> type;

    public EntityTypeFilter(Class<? extends Entity> type) {
        this.type = type;
    }

    public EntityTypeFilter(EntityType type) {
        this(type.getEntityClass());
    }

    public Class<? extends Entity> getEntityType() {
        return type;
    }

    @Override
    public boolean matches(IEntityTypeQuery query) {
        return type.isAssignableFrom(query.getEntityType());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{type=" + this.type.getSimpleName() + "}";
    }
}
