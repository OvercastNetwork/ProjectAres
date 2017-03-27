package tc.oc.pgm.rotation;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import tc.oc.commons.core.util.Lazy;
import tc.oc.pgm.map.PGMMap;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Represents a map rotation. Instances are completely immutable.
 */
public final class RotationState {
    /**
     * Creates a new RotationState instance.
     *
     * @param maps PGM maps in the rotation (order matters)
     * @param nextId Id of the next map in the rotation
     *
     * @throws NullPointerException if maps is null
     * @throws IllegalArgumentException if maps is empty or nextId is invalid
     */
    public RotationState(@Nonnull List<PGMMap> maps, int nextId) {
        Preconditions.checkNotNull(maps, "maps");
        Preconditions.checkArgument(!maps.isEmpty(), "must have at least one map");
        Preconditions.checkArgument(isNextIdValid(maps, nextId), "next id is invalid");

        this.maps = ImmutableList.copyOf(maps);
        this.nextId = nextId;
    }

    /**
     * Gets the list of maps in order that the rotation has.
     * @return Immutable ordered list of maps (guaranteed to be 1 or more)
     */
    public @Nonnull List<PGMMap> getMaps() {
        return this.maps;
    }

    
    private Lazy<Integer> averageNeededPlayers = Lazy.from(() ->
            (int) getMaps().stream().mapToInt(map -> map.getContext().playerLimitAverage()).average().orElse(0));
    
    /**
     * Gets the approximate number of players supposed to be playing the rotation maps.
     * @return Integer with average size of teams over all maps
     */
    public @Nonnull Integer getAverageNeededPlayers() {
        return averageNeededPlayers.get();
    }

    /**
     * Gets the next map in the rotation as specified by the next id.
     * @return Next map in rotation
     */
    public @Nonnull PGMMap getNext() {
        return this.maps.get(this.nextId);
    }

    /**
     * Gets the index of the next map.
     * @return Index of next map
     */
    public int getNextId() {
        return this.nextId;
    }

    /**
     * Skip n maps in the rotation
     * @param n Number of maps to skip
     * @return New rotation state instance with the modifications
     */
    public @Nonnull RotationState skip(int n) {
        int newNextId = (this.nextId + n) % this.maps.size();
        return new RotationState(this.maps, newNextId);
    }

    /**
     * Skip to the nth map in the rotation.
     * @param n Index of the next map
     * @return New rotation state instance with the modifications
     *
     * @throws IllegalArgumentException if n is invalid
     */
    public @Nonnull RotationState skipTo(int n) {
        Preconditions.checkArgument(isNextIdValid(this.maps, n), "n is invalid");

        return new RotationState(this.maps, n);
    }

    /**
     * Checks whether the given next id is valid for the list of maps.
     * @param maps List of rotation maps
     * @param newRotationId Next id to check
     * @return true if the next id is a valid index, false otherwise
     */
    public static boolean isNextIdValid(List<PGMMap> maps, int newRotationId) {
        return 0 <= newRotationId && newRotationId < maps.size();
    }

    protected final List<PGMMap> maps;
    protected final int nextId;
}
