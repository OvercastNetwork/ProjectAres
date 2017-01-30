package tc.oc.pgm.match;

import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.EntityLocation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.commons.bukkit.chat.Named;
import tc.oc.pgm.filters.query.IEntityQuery;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class MatchEntityState implements Named, IEntityQuery {
    protected final Match match;
    protected final Class<? extends Entity> entityClass;
    protected final EntityType entityType;
    protected final UUID uuid;
    protected final EntityLocation location;
    protected final @Nullable String customName;

    protected MatchEntityState(Match match, Class<? extends Entity> entityClass, UUID uuid, EntityLocation location, @Nullable String customName) {
        this.uuid = checkNotNull(uuid);
        this.match = checkNotNull(match);
        this.entityClass = checkNotNull(entityClass);
        this.location = checkNotNull(location);
        this.customName = customName;

        EntityType type = null;
        for(EntityType t : EntityType.values()) {
            if(t.getEntityClass().isAssignableFrom(entityClass)) {
                type = t;
                break;
            }
        }
        checkArgument(type != null, "Unknown entity class " + entityClass);
        this.entityType = type;
    }

    public static @Nullable MatchEntityState get(Entity entity) {
        Match match = Matches.get(entity.getWorld());
        String customName = entity instanceof Player ? null : entity.getCustomName();
        return match == null ? null : new MatchEntityState(match, entity.getClass(), entity.getUniqueId(), entity.getEntityLocation(), customName);
    }

    @Override
    public Match getMatch() {
        return match;
    }

    @Override
    public Class<? extends Entity> getEntityType() {
        return entityClass;
    }

    @Override
    public EntityLocation getEntityLocation() {
        return location;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public BaseComponent getStyledName(NameStyle style) {
        if(customName != null) {
            return new TextComponent(customName);
        } else {
            return new TranslatableComponent("entity." + entityType.getName() + ".name");
        }
    }

    public boolean isEntity(Entity entity) {
        return uuid.equals(entity.getUniqueId());
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(!MatchEntityState.class.equals(obj.getClass())) return false;
        final MatchEntityState that = (MatchEntityState) obj;
        return Objects.equals(uuid, that.uuid) &&
               Objects.equals(match, that.match);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, match);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
               "{match=" + match +
               ", type=" + entityClass +
               ", uuid=" + uuid +
               '}';
    }
}
