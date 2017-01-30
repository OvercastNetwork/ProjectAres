package tc.oc.commons.core.event;

import javax.inject.Singleton;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionHandler;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class EventBusModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(SubscriberExceptionHandler.class).to(EventExceptionHandler.class);
    }

    @Provides @Singleton
    EventBus eventBus(SubscriberExceptionHandler exceptionHandler) {
        return new ReentrantEventBus(exceptionHandler);
    }
}
