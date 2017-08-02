package tc.oc.commons.core.logging;

import java.util.logging.Logger;

public class SimpleLoggerFactory extends ClassLoggerFactory {

    @Override
    public Logger defaultLogger(Class<?> klass) {
        return defaultLogger();
    }

    @Override
    public Logger defaultLogger() {
        return Logger.getLogger("");
    }
}
