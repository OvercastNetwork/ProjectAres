package tc.oc.pgm.tracker.damage;

import java.util.Optional;
import javax.annotation.Nullable;

import org.bukkit.event.entity.EntityDamageEvent;
import tc.oc.commons.core.inspect.Inspectable;
import tc.oc.pgm.match.ParticipantState;

import static com.google.common.base.Preconditions.checkNotNull;

public class GenericDamageInfo extends Inspectable.Impl implements DamageInfo, CauseInfo {

    @Inspect private final @Nullable PhysicalInfo damager;
    @Inspect private final EntityDamageEvent.DamageCause damageType;

    public GenericDamageInfo(EntityDamageEvent.DamageCause damageType, @Nullable PhysicalInfo damager) {
        this.damageType = checkNotNull(damageType);
        this.damager = damager;
    }

    public GenericDamageInfo(EntityDamageEvent.DamageCause damageType) {
        this(damageType, null);
    }

    public Optional<PhysicalInfo> damager() {
        return Optional.ofNullable(damager);
    }

    @Override
    public @Nullable PhysicalInfo getCause() {
        return damager;
    }

    public EntityDamageEvent.DamageCause getDamageType() {
        return damageType;
    }

    @Override
    public @Nullable ParticipantState getAttacker() {
        return damager == null ? null : damager.getOwner();
    }
}
