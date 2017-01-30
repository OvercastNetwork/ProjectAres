package tc.oc.commons.core.logging;

import java.util.logging.Logger;
import javax.annotation.Nullable;

public abstract class ClassLoggerFactory implements Loggers {

    public Logger get(Logger parent, Class<?> klass, @Nullable String instanceKey) {
        return ClassLogger.get(parent, klass, instanceKey);
    }

    public @Nullable Logger find(Class<?> klass, String instanceKey) {
        return ClassLogger.find(defaultLogger(klass), klass, instanceKey);
    }
}
