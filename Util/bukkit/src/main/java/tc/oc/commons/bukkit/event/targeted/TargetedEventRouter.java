package tc.oc.commons.bukkit.event.targeted;

import java.util.stream.Stream;

import org.bukkit.event.Listener;

/**
 * An object that knows which {@link Listener}s should receive a given targeted event instance.
 * Routers are registered at startup through a {@link TargetedEventRouterBinder}.
 */
public interface TargetedEventRouter<E> {

    /**
     * Return the listeners that should receive the given targeted event.
     * These listeners must already be registered with the {@link TargetedEventBus}.
     */
    Stream<Listener> listeners(E event);
}
