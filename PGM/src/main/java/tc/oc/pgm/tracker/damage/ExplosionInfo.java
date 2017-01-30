package tc.oc.pgm.tracker.damage;

import java.util.Optional;
import javax.annotation.Nullable;

import org.bukkit.Location;
import tc.oc.commons.core.inspect.Inspectable;
import tc.oc.pgm.match.ParticipantState;

import static com.google.common.base.Preconditions.checkNotNull;

public class ExplosionInfo extends Inspectable.Impl implements DamageInfo, RangedInfo, CauseInfo {

    @Inspect private final PhysicalInfo explosive;

    public ExplosionInfo(PhysicalInfo explosive) {
        this.explosive = checkNotNull(explosive);
    }

    @Override
    public Optional<PhysicalInfo> damager() {
        return Optional.of(explosive);
    }

    public PhysicalInfo getExplosive() {
        return explosive;
    }

    @Override
    public TrackerInfo getCause() {
        return getExplosive();
    }

    @Override
    public @Nullable Location getOrigin() {
        return explosive instanceof RangedInfo ? ((RangedInfo) explosive).getOrigin()
                                               : null;
    }

    @Override
    public @Nullable ParticipantState getAttacker() {
        return explosive == null ? null : explosive.getOwner();
    }
}
