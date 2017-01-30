package tc.oc.api.model;

import javax.annotation.Nullable;

import tc.oc.api.docs.virtual.Model;

@FunctionalInterface
public interface ModelHandler<T extends Model> {
    /**
     * Called when any instance of the model is created, modified, or deleted.
     *
     * @param before    State before the change, or null if instance is being created
     * @param after     State after the change, or null if instance is being hard-deleted
     * @param latest    Most recent non-deleted state (equal to before on deletion, otherwise equal to after)
     */
    void modelUpdated(@Nullable T before, @Nullable T after, T latest);
}
