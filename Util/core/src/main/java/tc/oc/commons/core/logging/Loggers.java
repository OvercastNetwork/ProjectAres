package tc.oc.commons.core.logging;

import java.util.logging.Logger;
import javax.annotation.Nullable;

public interface Loggers {

    Logger get(Logger parent, Class<?> klass, @Nullable String instanceKey);

    default Logger get(Class<?> klass, @Nullable String instanceKey) {
        return get(defaultLogger(klass), klass, instanceKey);
    }

    default Logger get(Class<?> klass) {
        return get(klass, null);
    }

    @Nullable Logger find(Class<?> klass, @Nullable String instanceKey);

    default @Nullable Logger find(Class<?> klass) {
        return find(klass, null);
    }

    /**
     * Return an existing logger suitable for the given class, without creating any new loggers.
     */
    Logger defaultLogger(Class<?> klass);

    /**
     * Return a logger suitable for any class.
     */
    Logger defaultLogger();
}
