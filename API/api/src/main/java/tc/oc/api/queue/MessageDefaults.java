package tc.oc.api.queue;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface MessageDefaults {
    @Retention(RetentionPolicy.RUNTIME)
    @interface RoutingKey {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Persistent {
        boolean value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface ExpirationMillis {
        int value();
    }
}
