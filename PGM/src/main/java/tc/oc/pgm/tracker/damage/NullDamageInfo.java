package tc.oc.pgm.tracker.damage;

import javax.annotation.Nullable;

import tc.oc.commons.core.inspect.Inspectable;
import tc.oc.pgm.match.ParticipantState;

/**
 * Returned by the master damage resolver to indicate that the damage is invalid,
 * i.e. because one of the players involved was not participating.
 */
public class NullDamageInfo extends Inspectable.Impl implements DamageInfo {
    @Override
    public @Nullable ParticipantState getAttacker() {
        return null;
    }
}
