package tc.oc.commons.bukkit.event;

import java.lang.reflect.Method;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;

/**
 * Scans classes for standard Bukkit event handler methods, which are annotated with {@link EventHandler}
 */
public class BukkitEventHandlerScanner extends EventHandlerScanner<Event, EventHandler, EventHandlerInfo<? extends Event>> {
    @Override
    protected EventHandlerInfo<? extends Event> createHandlerInfo(Method method, Class<? extends Event> eventType, EventHandler annotation) {
        return new EventHandlerInfo<>(new EventKey<>(eventType,
                                                     annotation.priority()),
                                      method,
                                      annotation.ignoreCancelled());
    }
}
