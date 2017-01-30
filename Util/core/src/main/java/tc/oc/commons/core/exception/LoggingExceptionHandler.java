package tc.oc.commons.core.exception;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;

import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.util.StackTrace;

/**
 * TODO: Merge with {@link tc.oc.exception.LoggingExceptionHandler}
 */
public class LoggingExceptionHandler implements ExceptionHandler<Throwable>, Thread.UncaughtExceptionHandler {

    private final Loggers loggers;

    @Inject public LoggingExceptionHandler(Loggers loggers) {
        this.loggers = loggers;
    }

    protected String messagePrefix() {
        return "Unhandled exception";
    }

    @Override
    public void handleException(Throwable throwable, @Nullable Object source, @Nullable StackTrace trace) {
        String message = messagePrefix();

        if(source != null) {
            message += " from " + source;
        }

        if(trace != null) {
            message += " created...\n" + trace;
        }

        final Logger logger = source == null ? loggers.defaultLogger()
                                             : loggers.defaultLogger(source.getClass());

        logger.log(Level.SEVERE, message, throwable);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        handleException(throwable, thread);
    }
}
