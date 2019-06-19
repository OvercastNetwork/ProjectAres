package tc.oc.pgm.rotation;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.time.Instant;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * Abstract implementation of {@link RotationProvider}.
 */
public abstract class AbstractRotationProvider implements RotationProvider {
    @Override
    public @Nonnull Map<String, RotationState> getRotations() {
        synchronized(this) {
            return ImmutableMap.copyOf(this.rotations);
        }
    }

    @Override
    public @Nullable RotationState getRotation(@Nonnull String name) {
        Preconditions.checkNotNull(name, "rotation name");
        synchronized(this) {
            RotationState state = this.rotations.get(name);
            if (state != null) return state;

            for (String key : this.rotations.keySet()) {
                if (key.equalsIgnoreCase(name)) {
                    state = this.rotations.get(key);
                    break;
                }
            }

            return state;
        }
    }

    protected boolean setRotation(@Nonnull String name, @Nonnull RotationState rotation) {
        Preconditions.checkNotNull(name, "name");
        Preconditions.checkNotNull(rotation, "rotation");

        return this.setRotation(name, rotation, Instant.now());
    }

    protected boolean setRotation(@Nonnull String name, @Nonnull RotationState rotation, @Nonnull Instant loadTime) {
        Preconditions.checkNotNull(name, "rotation name");
        Preconditions.checkNotNull(rotation, "rotation");
        Preconditions.checkNotNull(loadTime, "rotation load time");

        synchronized(this) {
            Instant lastLoaded = this.rotationsLastLoadedTime.get(name);
            if(lastLoaded != null && lastLoaded.isAfter(loadTime)) {
                return false;
            }

            this.rotations.put(name, rotation);
            this.rotationsLastLoadedTime.put(name, loadTime);
        }

        return true;
    }

    private final Map<String, RotationState> rotations = Maps.newHashMap();
    private final Map<String, Instant> rotationsLastLoadedTime = Maps.newHashMap();
}
