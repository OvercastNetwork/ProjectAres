package tc.oc.pgm.rotation;

import java.util.List;

import javax.annotation.Nonnull;

import tc.oc.pgm.map.PGMMap;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Transformation that inserts a specified map at a specified index.
 */
public class InsertTransformation implements RotationTransformation {
    public InsertTransformation(@Nonnull PGMMap map, int index) {
        Preconditions.checkNotNull(map, "map");
        Preconditions.checkArgument(index >= 0, "index must not be negative");

        this.map = map;
        this.index = index;
    }

    public @Nonnull PGMMap getInsertedMap() {
        return this.map;
    }

    public @Nonnull int getInsertedIndex() {
        return this.index;
    }

    @Override
    public @Nonnull RotationState apply(@Nonnull RotationState state) {
        Preconditions.checkNotNull(state, "rotation state");

        // insert the map at the given position
        List<PGMMap> maps = Lists.newArrayList(state.getMaps());
        if(this.index > maps.size()) {
            return state;
        }
        maps.add(this.index, this.map);

        // if the map was inserted before the next id, increase the next id so
        // it still points to the same map as before
        int nextId = state.getNextId();
        if(nextId > this.index) {
            nextId++;
        }

        return new RotationState(maps, nextId);
    }

    protected final @Nonnull PGMMap map;
    protected final int index;
}
