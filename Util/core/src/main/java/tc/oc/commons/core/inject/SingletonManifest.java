package tc.oc.commons.core.inject;

/**
 * A {@link com.google.inject.Module} that compares equal to any other
 * module of the same type, or a subtype. This ensures that only one
 * instance of the module is ever installed.
 */
public abstract class SingletonManifest extends Manifest {
    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && getClass().equals(obj.getClass());
    }
}
