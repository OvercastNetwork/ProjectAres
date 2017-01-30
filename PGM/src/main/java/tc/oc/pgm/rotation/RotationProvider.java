package tc.oc.pgm.rotation;

import java.util.Map;
import java.util.concurrent.Future;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a (persistent) store for rotations.
 */
public interface RotationProvider {
    /**
     * Gets all the stored rotations that have been loaded.
     * @return Immutable map of rotation name to rotation state
     */
    @Nonnull Map<String, RotationState> getRotations();

    /**
     * Gets the stored (cached) rotation if it is available.
     * @param name Name of the rotation
     * @return Stored rotation or null if there is not one loaded
     */
    @Nullable RotationState getRotation(@Nonnull String name);

    /**
     * Prompts the provider to load all the rotations it has.
     * @return Future that will complete when the rotations have loaded
     */
    @Nonnull Future<?> loadRotations();

    /**
     * Prompts the provider to save the given rotation.
     * @param name Name of the rotation
     * @param newRotation New rotation to save
     * @return Future that will complete when the rotation is saved
     */
    @Nonnull Future<?> saveRotation(@Nonnull String name, @Nonnull RotationState newRotation);
}
