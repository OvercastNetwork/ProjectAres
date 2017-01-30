package tc.oc.pgm.tracker.damage;

import javax.annotation.Nullable;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import tc.oc.commons.bukkit.util.NMSHacks;
import tc.oc.commons.core.chat.Components;
import tc.oc.pgm.match.ParticipantState;

public class EntityInfo extends OwnerInfoBase implements PhysicalInfo {

    @Inspect private final EntityType entityType;
    @Inspect private final Class<? extends Entity> entityClass;
    @Inspect private final @Nullable String customName;
    private final String nameKey;

    public EntityInfo(Entity entity, @Nullable ParticipantState owner) {
        super(owner);
        this.entityType = entity.getType();
        this.entityClass = entity.getClass();
        this.customName = entity.getCustomName();
        this.nameKey = NMSHacks.getTranslationKey(entity);
    }

    public Class<? extends Entity> getEntityClass() {
        return entityClass;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public @Nullable String getCustomName() {
        return customName;
    }

    @Override
    public String getIdentifier() {
        return getEntityType().getName();
    }

    @Override
    public BaseComponent getLocalizedName() {
        return getCustomName() != null ? Components.fromLegacyText(getCustomName())
                                       : new TranslatableComponent(nameKey);
    }
}
