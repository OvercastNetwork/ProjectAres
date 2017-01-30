package tc.oc.pgm.tracker.damage;

import java.util.Optional;
import javax.annotation.Nullable;

import tc.oc.pgm.match.ParticipantState;

public interface DamageInfo extends TrackerInfo {

    default Optional<PhysicalInfo> damager() {
        return Optional.empty();
    }

    @Nullable ParticipantState getAttacker();

    default Optional<ParticipantState> attacker() {
        return Optional.ofNullable(getAttacker());
    }
}
