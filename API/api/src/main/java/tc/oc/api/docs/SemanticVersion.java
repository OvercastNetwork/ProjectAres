package tc.oc.api.docs;

public class SemanticVersion {
    protected final int major;
    protected final int minor;
    protected final int patch;

    public SemanticVersion(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public int major() {
        return this.major;
    }

    public int minor() {
        return this.minor;
    }

    public int patch() {
        return this.patch;
    }

    @Override
    public String toString() {
        if(patch == 0) {
            return major + "." + minor;
        } else {
            return major + "." + minor + "." + patch;
        }
    }

    /**
     * Return true if the major versions match and the minor version
     * and patch levels are less or equal to the given version
     */
    public boolean isNoNewerThan(SemanticVersion spec) {
        return this.major == spec.major &&
               (this.minor < spec.minor ||
                (this.minor == spec.minor &&
                 this.patch <= spec.patch));
    }

    /**
     * Return true if the major versions match and the minor version
     * and patch levels are greater than the given version
     */
    public boolean isNewerThan(SemanticVersion spec) {
        return this.major == spec.major &&
               (this.minor > spec.minor ||
                (this.minor == spec.minor &&
                 this.patch > spec.patch));
    }

    /**
     * Return true if the major versions match and the minor version
     * and patch levels are greater or equal to the given version
     */
    public boolean isNoOlderThan(SemanticVersion spec) {
        return this.major == spec.major &&
               (this.minor > spec.minor ||
                (this.minor == spec.minor &&
                 this.patch >= spec.patch));
    }

    /**
     * Return true if the major versions match and the minor version
     * and patch levels are less than the given version
     */
    public boolean isOlderThan(SemanticVersion spec) {
        return this.major == spec.major &&
               (this.minor < spec.minor ||
                (this.minor == spec.minor &&
                 this.patch < spec.patch));
    }

    public int[] toArray() {
        return new int[] {major, minor, patch};
    }
}
