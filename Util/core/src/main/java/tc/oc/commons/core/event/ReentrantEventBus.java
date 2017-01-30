package tc.oc.commons.core.event;

import java.lang.reflect.Field;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionHandler;

public class ReentrantEventBus extends EventBus {

    private final ThreadLocal<Boolean> isDispatching;

    public ReentrantEventBus() {
        this.isDispatching = getIsDispatching();
    }

    public ReentrantEventBus(String identifier) {
        super(identifier);
        this.isDispatching = getIsDispatching();
    }

    public ReentrantEventBus(SubscriberExceptionHandler subscriberExceptionHandler) {
        super(subscriberExceptionHandler);
        this.isDispatching = getIsDispatching();
    }

    private ThreadLocal<Boolean> getIsDispatching() {
        try {
            final Field field = EventBus.class.getDeclaredField("isDispatching");
            field.setAccessible(true);
            return (ThreadLocal<Boolean>) field.get(this);
        } catch(IllegalAccessException | NoSuchFieldException e) {
            throw new IllegalStateException();
        }
    }

    /**
     * HACK: Trick the superclass into allowing nested events
     *
     * EventBus is actually more of an event queue. If ANY event is posted inside
     * another event, it will be queued and dispatched only after the outer
     * event has finished dispatching. If your event posting code can't be sure
     * whether not it is an outer-most event, then it has to handle both the
     * possibility that handlers are called before the post returns AND the
     * possibility that they are queued and called at some arbitrary later time.
     * That seems pretty ridiculous to me.
     *
     * In any case, it is not the type of event bus that we need, and this was
     * discovered too late. So, we use reflection here to reset the thread-local
     * used to detect reentrancy before every event post, so the handlers are
     * always called immediately.
     */
    @Override
    public void post(Object event) {
        isDispatching.set(false);
        super.post(event);
    }
}
