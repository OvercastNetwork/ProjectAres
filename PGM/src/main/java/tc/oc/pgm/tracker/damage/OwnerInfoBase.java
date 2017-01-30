package tc.oc.pgm.tracker.damage;

import javax.annotation.Nullable;

import tc.oc.commons.core.inspect.Inspectable;
import tc.oc.pgm.match.ParticipantState;

public abstract class OwnerInfoBase extends Inspectable.Impl implements OwnerInfo {

    @Inspect private final @Nullable ParticipantState owner;

    public OwnerInfoBase(@Nullable ParticipantState owner) {
        this.owner = owner;
    }

    @Override
    public @Nullable ParticipantState getOwner() {
        return owner;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{owner=" + getOwner() + "}";
    }
}
