package tc.oc.pgm.rotation;

import java.util.List;

import javax.annotation.Nonnull;

import tc.oc.pgm.map.PGMMap;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Represents a transformation that appends a map to the end of the rotation.
 */
public class AppendTransformation implements RotationTransformation {
    public AppendTransformation(@Nonnull PGMMap map) {
        Preconditions.checkNotNull(map, "map");

        this.map = map;
    }

    public @Nonnull PGMMap getAppendedMap() {
        return this.map;
    }

    @Override
    public @Nonnull RotationState apply(RotationState state) {
        Preconditions.checkNotNull(state, "rotation state");

        // append map to maps array
        List<PGMMap> maps = Lists.newArrayList(state.getMaps());
        maps.add(this.map);

        // if the next id was pointed to the beginning, change it to the new map
        int nextId = state.getNextId();
        if(nextId == 0) {
            nextId = maps.size() - 1;
        }

        return new RotationState(maps, nextId);
    }

    protected final @Nonnull PGMMap map;
}
