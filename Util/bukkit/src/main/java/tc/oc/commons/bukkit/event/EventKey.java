package tc.oc.commons.bukkit.event;

import java.util.Objects;

import org.bukkit.event.EventPriority;

/**
 * Combines an event type and priority level into a single value. Useful as a cache key.
 */
public class EventKey<T> {
    final Class<T> event;
    final EventPriority priority;

    public EventKey(Class<T> event, EventPriority priority) {
        this.event = event;
        this.priority = priority;
    }

    public Class<T> event() {
        return event;
    }

    public EventPriority priority() {
        return priority;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof EventKey)) return false;
        EventKey eventKey = (EventKey) o;
        return Objects.equals(event, eventKey.event) &&
               priority == eventKey.priority;
    }

    @Override
    public int hashCode() {
        return Objects.hash(event, priority);
    }
}
