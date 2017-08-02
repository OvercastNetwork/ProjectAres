package tc.oc.commons.bukkit.event.targeted;

import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.SetMultimap;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventExecutor;
import org.bukkit.event.EventHandlerMeta;
import org.bukkit.event.EventRegistry;
import tc.oc.commons.bukkit.event.EventHandlerInfo;
import tc.oc.commons.bukkit.event.EventKey;
import tc.oc.commons.core.exception.ExceptionHandler;
import tc.oc.commons.core.exception.InvalidMemberException;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.commons.core.util.CacheUtils;
import tc.oc.commons.core.util.TypeMap;
import tc.oc.minecraft.api.event.Listener;

/**
 * Listens for targeted events and dispatches them to registered {@link TargetedEventHandler}s
 */
@Singleton
public class TargetedEventBusImpl implements PluginFacet, TargetedEventBus {

    private final ExceptionHandler exceptionHandler;
    private final EventRegistry eventRegistry;
    private final Set<EventKey> events = new HashSet<>();
    private final TypeMap<Object, TargetedEventRouter<?>> routers;
    private final LoadingCache<Class<? extends Listener>, SetMultimap<EventKey<? extends Event>, EventHandlerInfo<? extends Event>>> listenerCache;

    @Inject TargetedEventBusImpl(ExceptionHandler exceptionHandler, EventRegistry eventRegistry, TargetedEventHandlerScanner eventHandlerScanner, TypeMap<Object, TargetedEventRouter<?>> routers) {
        this.exceptionHandler = exceptionHandler;
        this.eventRegistry = eventRegistry;
        this.routers = routers;

        // Cache of handler methods per listener type
        this.listenerCache = CacheUtils.newCache(listener -> {
            final SetMultimap<EventKey<? extends Event>, EventHandlerInfo<? extends Event>> handlers = eventHandlerScanner.findEventHandlers(listener);
            for(EventHandlerInfo<? extends Event> info : handlers.values()) {
                if(this.routers.allAssignableFrom(info.event()).isEmpty()) {
                    throw new InvalidMemberException(
                        info.method(),
                        "No router registered for targeted event type " + info.event().getName()
                    );
                }
            }
            return handlers;
        });
    }

    @Override
    public void registerListener(Listener listener) {
        registerListenerType(listener.getClass());
    }

    @Override
    public void unregisterListener(Listener listener) {
    }

    /**
     * Generate MethodHandles for all {@link TargetedEventHandler}s in the given listener class,
     * and ensure we are listening for their event types. If the given class has already been
     * registered, do nothing.
     */
    private void registerListenerType(Class<? extends Listener> listenerType) {
        listenerCache.getUnchecked(listenerType).keySet().forEach(this::registerEvent);
    }

    /**
     * Start listening for the given event/priority, if we aren't already.
     */
    private void registerEvent(EventKey<? extends Event> key) {
        // The first time we see a particular event at a particular priority level, start listening for it
        if(events.add(key)) {
            Event.register(eventRegistry.bindHandler(new EventHandlerMeta<>(key.event(), key.priority(), false), this, new Executor(key)));
        }
    }

    private class Executor implements EventExecutor {

        final EventKey<? extends Event> key;
        Executor(EventKey<? extends Event> key) { this.key = key; }

        @Override
        public void execute(Listener dispatcher, Event event) throws EventException {
            final Set<Listener> listeners = new HashSet<>();

            for(TargetedEventRouter router : routers.allAssignableFrom(event.getClass())) {
                router.listeners(event).forEach(listener -> listeners.add((Listener) listener));
            }

            for(Listener listener : listeners) {
                final SetMultimap<EventKey<? extends Event>, EventHandlerInfo<? extends Event>> handlerMap = listenerCache.getIfPresent(listener.getClass());

                if(handlerMap == null) {
                    exceptionHandler.handleException(new Exception("Targeted event listener is not registered: " + listener));
                    continue;
                }

                for(EventHandlerInfo<? extends Event> handlerInfo : handlerMap.get(key)) {
                    if(handlerInfo.method().getParameterTypes()[0].isInstance(event) &&
                       !(handlerInfo.ignoreCancelled() && event.isCancelled())) {
                        try {
                            handlerInfo.methodHandle().invokeWithArguments(listener, event);
                        } catch(Throwable throwable) {
                            exceptionHandler.handleException(throwable, null, null);
                        }
                    }
                }
            }
        }
    }
}

