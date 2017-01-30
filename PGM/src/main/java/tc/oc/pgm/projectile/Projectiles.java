package tc.oc.pgm.projectile;

import javax.annotation.Nullable;

import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.MetadataValue;
import tc.oc.commons.bukkit.item.StringItemTag;
import tc.oc.commons.core.util.Threadable;
import tc.oc.pgm.PGM;
import tc.oc.pgm.features.FeatureDefinitionContext;

public interface Projectiles {

    StringItemTag ITEM_TAG = new StringItemTag("projectile", null);
    String METADATA_KEY = "projectileDefinition";

    // Holds the definition for a projectile from immediately before launch to
    // just after the definition is attached as metadata. Bukkit fires various
    // creation events before we have a chance to attach the metadata, and this
    // is how we make the definition available from within those events. The
    // ThreadLocal is not necessary, but it doesn't hurt and it's good form.
    Threadable<ProjectileDefinition> launchingDefinition = new Threadable<>();

    static @Nullable String getProjectileId(ItemStack item) {
        return ITEM_TAG.get(item);
    }

    static void setProjectileId(ItemMeta item, String id) {
        ITEM_TAG.set(item, id);
    }

    static @Nullable ProjectileDefinition getProjectileDefinition(FeatureDefinitionContext context, ItemStack item) {
        final String id = getProjectileId(item);
        return id == null ? null : context.get(id, ProjectileDefinition.class);
    }

    static @Nullable ProjectileDefinition getProjectileDefinition(Entity entity) {
        final MetadataValue metadataValue = entity.getMetadata(METADATA_KEY, PGM.get());
        return metadataValue == null ? null : (ProjectileDefinition) metadataValue.value();
    }

    static @Nullable ProjectileDefinition launchingProjectileDefinition(Entity entity) {
        final ProjectileDefinition definition = getProjectileDefinition(entity);
        return definition != null ? definition : launchingDefinition.get();
    }
}
