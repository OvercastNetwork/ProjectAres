package tc.oc.pgm.rotation;

import javax.annotation.Nonnull;


/**
 * Represents a transformation to a {@link RotationState} object.
 */
public interface RotationTransformation {
    /**
     * Applies the stored transformation to the given argument.
     * @param state RotationState to transform
     * @return New RotationState instance with the specified transformations
     *
     * @throws NullPointerException if state is null
     */
    public @Nonnull RotationState apply(@Nonnull RotationState state);
}
