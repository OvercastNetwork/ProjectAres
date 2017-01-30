package tc.oc.commons.bukkit.util;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public final class BukkitEvents {
    private BukkitEvents() { }

    public static boolean isCancelled(Event event) {
        return event instanceof Cancellable && ((Cancellable) event).isCancelled();
    }
}
