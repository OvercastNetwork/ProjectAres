package tc.oc.pgm.rotation;

import java.util.List;

import javax.annotation.Nonnull;

import tc.oc.pgm.map.PGMMap;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Transformation that removes all instances of a specific map from the
 * rotation.
 */
public class RemoveAllTransformation implements RotationTransformation {
    public RemoveAllTransformation(@Nonnull PGMMap map) {
        Preconditions.checkNotNull(map, "map");

        this.map = map;
    }

    public @Nonnull PGMMap getRemovedMap() {
        return this.map;
    }

    @Override
    public @Nonnull RotationState apply(@Nonnull RotationState state) {
        Preconditions.checkNotNull(state, "rotation state");

        List<PGMMap> maps = Lists.newArrayList(state.getMaps());
        int nextId = state.getNextId();

        for(int i = 0; i < maps.size() && maps.size() > 1; i++) {
            if(maps.get(i) == this.map) {
                // need to remove
                maps.remove(i);
                if(nextId >= maps.size()) {
                    nextId = 0;
                } else if (i < nextId) {
                    nextId--;
                }
            }
        }

        return new RotationState(maps, nextId);
    }

    protected final @Nonnull PGMMap map;
}
