package tc.oc.pgm.module;

public enum ProvisionResult {
    PRESENT,    // Module loaded successfully
    ABSENT,     // Module declined to load
    FAILED,     // Module threw a recoverable exception while trying to load
}
