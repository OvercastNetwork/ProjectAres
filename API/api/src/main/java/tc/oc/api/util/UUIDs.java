package tc.oc.api.util;

import java.util.UUID;

public abstract class UUIDs {
    public static String normalize(UUID uuid) {
        return uuid == null ? null : uuid.toString().replace("-", "");
    }

    public static UUID parse(String s) {
        if(s.length() != 32) throw new IllegalArgumentException("Invalid UUID: " + s);
        return UUID.fromString(s.substring(0, 8) + '-' +
                               s.substring(8, 12) + '-' +
                               s.substring(12, 16) + '-' +
                               s.substring(16, 20) + '-' +
                               s.substring(20, 32));
    }
}
