package tc.oc.commons.bukkit.event.targeted;

import java.lang.reflect.Method;

import org.bukkit.event.Event;
import tc.oc.commons.bukkit.event.EventHandlerInfo;
import tc.oc.commons.bukkit.event.EventHandlerScanner;
import tc.oc.commons.bukkit.event.EventKey;

/**
 * Scans classes for targeted event handler methods, which are annotated with {@link TargetedEventHandler}
 */
public class TargetedEventHandlerScanner extends EventHandlerScanner<Event, TargetedEventHandler, EventHandlerInfo<? extends Event>> {
    @Override
    protected EventHandlerInfo<? extends Event> createHandlerInfo(Method method, Class<? extends Event> eventType, TargetedEventHandler annotation) {
        return new EventHandlerInfo<>(new EventKey<>(eventType,
                                                     annotation.priority()),
                                      method,
                                      annotation.ignoreCancelled());
    }
}
