package tc.oc.commons.bukkit.event;

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;
import java.util.function.Consumer;

import org.bukkit.event.EventPriority;
import tc.oc.minecraft.api.event.Listener;

public interface EventSubscriber<E> {

    Class<Listener> listener();

    EventKey<E> key();

    default Class<E> event() {
        return key().event();
    }

    Optional<AnnotatedElement> element();

    default EventPriority priority() {
        return key().priority();
    }

    boolean ignoreCancelled();

    Consumer<E> bindTo(Listener listener);
}
