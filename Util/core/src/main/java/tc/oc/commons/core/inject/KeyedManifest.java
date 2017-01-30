package tc.oc.commons.core.inject;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link com.google.inject.Module} that implements equality testing
 * with an explicit key passed to the constructor. This can be used to
 * make two different modules compare equal, ensuring that only one of
 * them is installed.
 */
public abstract class KeyedManifest extends Manifest {

    /** Upper bound of types that can be equal to this object */
    private final Class<? extends KeyedManifest> selfType;

    protected KeyedManifest() {
        this(null);
    }

    protected KeyedManifest(@Nullable Class<? extends KeyedManifest> selfType) {
        this.selfType = selfType != null ? selfType : getClass();
    }

    protected abstract Object manifestKey();

    @Override public boolean equals(Object that) {
        return this == that || (selfType.isInstance(that) && manifestKey().equals(((KeyedManifest) that).manifestKey()));
    }

    @Override public int hashCode() {
        return manifestKey().hashCode();
    }

    public static class Impl extends KeyedManifest {
        private final Object key;

        public Impl(Object key) {
            this.key = checkNotNull(key);
        }

        @Override
        protected Object manifestKey() {
            return key;
        }
    }
}
