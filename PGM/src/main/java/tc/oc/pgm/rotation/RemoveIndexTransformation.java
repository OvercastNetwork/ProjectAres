package tc.oc.pgm.rotation;

import java.util.List;

import javax.annotation.Nonnull;

import tc.oc.pgm.map.PGMMap;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Transformation that removes the map at a specific index.
 */
public class RemoveIndexTransformation implements RotationTransformation {
    public RemoveIndexTransformation(int index) {
        Preconditions.checkArgument(index >= 0, "index must not be negative");

        this.index = index;
    }

    public int getRemovedIndex() {
        return this.index;
    }

    @Override
    public @Nonnull RotationState apply(@Nonnull RotationState state) {
        Preconditions.checkNotNull(state, "rotation state");

        if(state.getMaps().size() > 1 && this.index < state.getMaps().size()) {
            List<PGMMap> maps = Lists.newArrayList(state.getMaps());
            maps.remove(this.index);

            int nextId = state.getNextId();
            if(nextId >= maps.size()) {
                nextId = 0;
            } else if (this.index < nextId) {
                nextId--;
            }

            return new RotationState(maps, nextId);
        }

        return state;
    }

    protected final int index;
}
