package tc.oc.commons.core.concurrent;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import tc.oc.commons.core.util.Comparables;

public final class ExecutorUtils {
    private ExecutorUtils() {}

    public static ListeningExecutorService asService(Executor executor) {
        if(executor instanceof ListeningExecutorService) {
            return (ListeningExecutorService) executor;
        } else if(executor instanceof ExecutorService) {
            return MoreExecutors.listeningDecorator((ExecutorService) executor);
        } else {
            return new ExecutorServiceDecorator(executor);
        }
    }

    /**
     * Await termination of the given {@link ExecutorService}. If it does not terminate within the
     * given time, log a severe error to the given logger, mentioning that the given technique was
     * used to try and shut it down.
     *
     * @return true if the executor terminated in time, false if it timed out
     */
    public static boolean awaitTermination(ExecutorService service, Logger logger, Duration timeout, String technique) {
        try {
            service.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS);
            return true;
        } catch(InterruptedException e) {
            logger.severe(technique + " shutdown of " + service + " did not complete within " + timeout);
            return false;
        }
    }

    /**
     * Shutdown the given executor using {@link ExecutorService#shutdownNow}, and log a severe
     * error if fails to terminate within the given time.
     *
     * @return true if the executor terminated in time, false if it timed out
     */
    public static boolean shutdownImpatiently(ExecutorService service, Logger logger, Duration timeout) {
        service.shutdownNow();
        return awaitTermination(service, logger, timeout, "Impatient");
    }

    /**
     * Shutdown the given executor using {@link ExecutorService#shutdown} and await termination.
     * If it fails to terminate before {@code patientTimeout}, log a severe error and then call
     * {@link #shutdownImpatiently} with {@code impatientTimeout}.
     *
     * This approach should only be used if the executor's tasks are expected to always terminate
     * on their own, or if the executor is expected to not have any running tasks. If tasks might
     * need to be interrupted, then just skip this step and call {@link #shutdownImpatiently}.
     *
     * Particularly, if tasks might be trying to sync with the thread that is doing the shutdown,
     * then they should always be interrupted in order to avoid deadlock.
     *
     * @return true if the executor terminated in time for either of the two attempts,
     *         false if it timed out for both
     */
    public static boolean shutdownPatiently(ExecutorService service, Logger logger, Duration patientTimeout, Duration impatientTimeout) {
        if(Comparables.greaterThan(patientTimeout, Duration.ZERO)) {
            service.shutdown();
            if(awaitTermination(service, logger, patientTimeout, "Patient")) return true;
        }
        return shutdownImpatiently(service, logger, impatientTimeout);
    }
}
