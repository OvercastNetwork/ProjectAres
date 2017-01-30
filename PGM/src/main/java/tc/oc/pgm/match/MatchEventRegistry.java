package tc.oc.pgm.match;

import java.lang.reflect.AnnotatedElement;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandlerMeta;
import org.bukkit.event.EventRegistry;
import org.bukkit.event.Listener;
import tc.oc.commons.bukkit.event.BukkitEventHandlerScanner;
import tc.oc.commons.bukkit.event.EventHandlerInfo;
import tc.oc.commons.core.exception.ExceptionHandler;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.util.CacheUtils;
import tc.oc.commons.core.util.ThrowingConsumer;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.MatchEvent;

/**
 * Basically re-implements most of Bukkit's event registration system in order to
 * implement per-match event filtering.
 *
 * TODO: This could be less hacky, and integrated with the whole targeted event system.
 */
@Singleton
public class MatchEventRegistry {

    // Thrown to silently skip an event
    private static class SkipEvent extends RuntimeException {}

    // Generated during registration, and used (internally) to start listening
    // with an actual match and listener instance
    private interface Loader {
        void load(Match match, Listener listener);
    }

    // Used internally to wrap events just before they are passed to a handler method
    private interface Wrapper {
        Event wrap(Match match, Event unwrapped);
    }

    private final Logger logger;
    private final EventRegistry eventRegistry;
    private final ExceptionHandler exceptionHandler;
    private final BukkitEventHandlerScanner bukkitScanner;

    private final LoadingCache<Class<? extends Listener>, Loader> loaders;

    @Inject MatchEventRegistry(Loggers loggers, EventRegistry eventRegistry, ExceptionHandler exceptionHandler, BukkitEventHandlerScanner bukkitScanner, Set<MatchListenerMeta> listeners) {
        this.logger = loggers.get(getClass());
        this.eventRegistry = eventRegistry;
        this.exceptionHandler = exceptionHandler;
        this.bukkitScanner = bukkitScanner;

        this.loaders = CacheUtils.newCache(this::createLoader);

        listeners.forEach(meta -> registerListener(meta.type(), meta.scope()));
    }

    /**
     * Register event handler methods on the given {@link Listener} type.
     *
     * This method can be called outside of any match, and will detect most static problems
     * right away. Known {@link Listener} types should be registered as early as possible,
     * in order to detect bugs. However, this is not required. Unregistered classes will be
     * registered automatically the first time an instance is passed to {@link #startListening}.
     *
     * Handler methods registered through this class have the following special behavior:
     *
     * If the event type extends {@link MatchEvent}, then only event instances belonging to
     * this match will be dispatched to the handler. Events from other matches will be
     * quietly ignored.
     *
     * All event handler methods registered with this bus must specify the {@link MatchScope}
     * to listen within. This can be done with a {@link ListenerScope} annotation on the
     * method itself, or on the method's declaring class, in which case it will apply to
     * all handler methods in that class.
     */
    public void registerListener(Class<? extends Listener> listenerType) {
        registerListener(listenerType, null);
    }

    public void registerListener(Class<? extends Listener> listenerType, @Nullable MatchScope listenerScope) {
        CacheUtils.getUnchecked(loaders, listenerType, () -> createLoader(listenerType, listenerScope));
    }

    /**
     * Start delivering events from the given match to the given listener. The listener's
     * class will be registered, if it isn't already.
     */
    public void startListening(Match match, Listener listener) {
        loaders.getUnchecked(listener.getClass()).load(match, listener);
    }

    /**
     * Stop delivering events to the given listener
     */
    public void stopListening(Match match, Listener listener) {
        eventRegistry.unregisterListener(listener);
    }

    private Loader createLoader(Class<? extends Listener> listener) {
        return createLoader(listener, null);
    }

    private Loader createLoader(Class<? extends Listener> listenerType, @Nullable MatchScope scope) {
        scope = listenerScope(listenerType, scope);

        if(logger.isLoggable(Level.FINE)) {
            logger.fine("Registering listener type " + listenerType.getName() + " in scope " + scope);
        }

        final ImmutableSet.Builder<Loader> builder = ImmutableSet.builder();
        for(EventHandlerInfo<? extends Event> handler : bukkitScanner.findEventHandlers(listenerType).values()) {
            builder.add(createLoader(handler, listenerScope(handler.method(), scope)));
        }
        final ImmutableSet<Loader> loaders = builder.build();
        return (match, listener) -> loaders.forEach(loader -> loader.load(match, listener));
    }

    private Loader createLoader(EventHandlerInfo<? extends Event> handler, @Nullable MatchScope matchScopeOrNull) {
        final MatchScope matchScope = matchScopeOrNull != null ? matchScopeOrNull : MatchScope.LOADED;
        final Class<? extends Event> eventClass;
        final Wrapper wrapper;

        eventClass = handler.key().event();

        if(logger.isLoggable(Level.FINE)) {
            logger.fine("    " + handler.method().getName() + "(" + eventClass.getSimpleName() + ") scope=" + matchScope);
        }

        if(MatchEvent.class.isAssignableFrom(handler.key().event())) {
            // Skip MatchEvents if they belong to a different match
            wrapper = (match, event) -> {
                if(!match.equals(((MatchEvent) event).getMatch())) throw new SkipEvent();
                return event;
            };
        } else {
            // Plain old event event, do nothing special
            // TODO: should we try to filter these too, if possible?
            wrapper = (match, event) -> event;
        }

        return (match, listener) -> {
            final ThrowingConsumer<Event, Throwable> boundMethod = (ThrowingConsumer<Event, Throwable>) handler.bindTo(listener);
            Event.register(eventRegistry.bindHandler(
                new EventHandlerMeta<>(eventClass, handler.priority(), handler.ignoreCancelled()),
                listener,
                (l, event) -> {
                    try {
                        if(eventClass.isInstance(event) && match.inScope(matchScope)) {
                            boundMethod.accept(wrapper.wrap(match, event));
                        }
                    } catch(SkipEvent ignored) {
                    } catch(Throwable throwable) {
                        exceptionHandler.handleException(throwable);
                    }
                }
            ));
        };
    }

    private static MatchScope listenerScope(AnnotatedElement thing, @Nullable MatchScope def) {
        final ListenerScope annotation = thing.getAnnotation(ListenerScope.class);
        return annotation == null ? def : annotation.value();
    }
}
