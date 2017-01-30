package tc.oc.commons.core.restart;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Fired by {@link RestartManager} when a server restart is requested. The server
 * will normally restart immediately after the event returns. To defer the restart,
 * call {@link #defer} to get a {@link Deferral} object, then call
 * {@link Deferral#resume} on that at some future time to resume the restart.
 */
public class RequestRestartEvent {

    private final Logger logger;
    private final String reason;
    private final int priority;
    private final Runnable callback;

    private final Set<Deferral> deferrals = new HashSet<>();

    public RequestRestartEvent(Logger logger, String reason, int priority, Runnable callback) {
        this.logger = logger;
        this.reason = reason;
        this.priority = priority;
        this.callback = callback;
    }

    public String reason() {
        return reason;
    }

    public int priority() {
        return priority;
    }

    Set<Deferral> deferrals() {
        return deferrals;
    }

    public Deferral defer(String deferrerName) {
        logger.info("Restart deferred by " + deferrerName);
        Deferral deferral = new Deferral(deferrerName);
        deferrals.add(deferral);
        return deferral;
    }

    public class Deferral {
        private final String deferrerName;

        public Deferral(String deferrerName) {
            this.deferrerName = deferrerName;
        }

        public RequestRestartEvent request() {
            return RequestRestartEvent.this;
        }

        public String deferrerName() {
            return deferrerName;
        }

        /**
         * Allow the deferred restart to proceed. After this method is called,
         * this object becomes useless and can be discarded.
         */
        public void resume() {
            if(deferrals.remove(this)) {
                logger.info("Restart resumed by " + deferrerName);
                callback.run();
            }
        }
    }
}
