package tc.oc.commons.core.event;

import java.util.logging.Level;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import tc.oc.commons.core.logging.Loggers;

@Singleton
public class EventExceptionHandler implements SubscriberExceptionHandler {

    private final Loggers loggers;

    @Inject EventExceptionHandler(Loggers loggers) {
        this.loggers = loggers;
    }

    @Override
    public void handleException(Throwable exception, SubscriberExceptionContext context) {
        loggers.get(context.getSubscriber().getClass()).log(
            Level.SEVERE,
            "Exception dispatching " + context.getEvent().getClass().getName() +
            " to " + context.getSubscriber().getClass().getName() +
            "#" + context.getSubscriberMethod().getName(),
            exception
        );
    }
}
