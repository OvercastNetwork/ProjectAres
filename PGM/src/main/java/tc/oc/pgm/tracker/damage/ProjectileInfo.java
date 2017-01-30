package tc.oc.pgm.tracker.damage;

import java.util.Optional;
import javax.annotation.Nullable;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Location;
import tc.oc.commons.core.chat.Components;
import tc.oc.commons.core.inspect.Inspectable;
import tc.oc.pgm.match.ParticipantState;

import static com.google.common.base.Preconditions.checkNotNull;

public class ProjectileInfo extends Inspectable.Impl implements PhysicalInfo, DamageInfo, RangedInfo {

    @Inspect private final PhysicalInfo projectile;
    @Inspect private final @Nullable PhysicalInfo shooter;
    @Inspect private final Location origin;
    @Inspect private final @Nullable String customName;

    public ProjectileInfo(PhysicalInfo projectile, @Nullable PhysicalInfo shooter, Location origin, @Nullable String customName) {
        this.projectile = checkNotNull(projectile);
        this.shooter = shooter;
        this.origin = checkNotNull(origin);
        this.customName = customName;
    }

    @Override
    public Optional<PhysicalInfo> damager() {
        return Optional.of(projectile);
    }

    public PhysicalInfo getProjectile() {
        return projectile;
    }

    public @Nullable PhysicalInfo getShooter() {
        return shooter;
    }

    @Override
    public Location getOrigin() {
        return this.origin;
    }

    @Override
    public @Nullable ParticipantState getOwner() {
        return shooter == null ? null : shooter.getOwner();
    }

    @Override
    public @Nullable ParticipantState getAttacker() {
        return getOwner();
    }

    @Override
    public String getIdentifier() {
        return getProjectile().getIdentifier();
    }

    @Override
    public BaseComponent getLocalizedName() {
        if(customName != null) {
            return Components.fromLegacyText(customName);
        } else if(getProjectile() instanceof PotionInfo) {
            // PotionInfo.getLocalizedName returns a potion name,
            // which doesn't work outside a potion death message.
            return new TranslatableComponent("item.potion.name");
        } else {
            return getProjectile().getLocalizedName();
        }
    }
}
