package tc.oc.commons.bukkit.suspend;

import java.util.Set;
import javax.inject.Inject;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerSuspendEvent;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.commons.core.util.ThrowingConsumer;
import tc.oc.minecraft.suspend.Suspendable;

import static com.google.common.base.Preconditions.checkState;

/**
 * Listens for {@link ServerSuspendEvent} and dispatches it to all {@link Suspendable}s
 */
public class SuspendListener implements PluginFacet, Listener {

    private final ThrowingConsumer<ServerSuspendEvent, EventException> yielder;

    @Inject SuspendListener(Set<Suspendable> suspendables) {
        ThrowingConsumer<ServerSuspendEvent, EventException> yielder = Event::yield;
        for(Suspendable suspendable : suspendables) {
            ThrowingConsumer<ServerSuspendEvent, EventException> next = yielder;
            yielder = event -> suspendable.suspend(() -> next.acceptThrows(event));
        }
        this.yielder = yielder;
    }

    @EventHandler
    void onSuspend(ServerSuspendEvent event) throws EventException {
        yielder.acceptThrows(event);
        checkState(!event.canYield(), "Suspendable didn't yield");
    }
}
