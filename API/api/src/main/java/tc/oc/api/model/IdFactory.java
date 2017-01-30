package tc.oc.api.model;

import java.util.UUID;

/**
 * TODO: This should probably be pluggable
 */
public class IdFactory {
    public String newId() {
        return UUID.randomUUID().toString();
    }
}
