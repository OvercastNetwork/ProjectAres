package tc.oc.commons.bukkit.event.targeted;

import tc.oc.minecraft.api.event.Listener;

/**
 * A targeted event is one that is delivered to some subset of listeners derived from the
 * event itself. In other words, a "scoped" or "contextual" event. Any type of event can be
 * targeted, without altering the code of the event itself.
 *
 * Several classes are involved in this system:
 *
 *     - {@link TargetedEventHandler} identifies event handler methods on listeners
 *     - {@link TargetedEventRouter} associates event instances with their listeners
 *     - {@link TargetedEventBus} is used to register targeted listeners with the system
 *
 * Every listener must be registered with this class at least once in order to receive targeted events.
 */
public interface TargetedEventBus {

    /**
     * Register the given listener to receive targeted events through its {@link TargetedEventHandler}s.
     * An error MAY be logged if an unregistered listener is resolved from a targeted event.
     *
     * Targeted listener registration is completely unrelated to Bukkit's global listener registration.
     * A listener can be used with both systems simultaneously, but it must register with each explicitly.
     */
    void registerListener(Listener listener);

    void unregisterListener(Listener listener);
}
