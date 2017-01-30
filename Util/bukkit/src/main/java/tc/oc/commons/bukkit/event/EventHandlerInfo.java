package tc.oc.commons.bukkit.event;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Optional;
import javax.annotation.Nullable;

import com.google.common.base.Throwables;
import tc.oc.commons.core.util.ThrowingConsumer;
import tc.oc.minecraft.api.event.Listener;

/**
 * Describes an event handler method independent from any particular annotation.
 */
public class EventHandlerInfo<E> implements EventSubscriber<E> {

    private final EventKey<E> key;
    private final Method method;
    private final boolean ignoreCancelled;
    private @Nullable MethodHandle methodHandle;

    public EventHandlerInfo(EventKey<E> key, Method method, boolean ignoreCancelled) {
        this.key = key;
        this.method = method;
        this.ignoreCancelled = ignoreCancelled;

        this.method.setAccessible(true);
    }

    @Override
    public Class<Listener> listener() {
        return (Class<Listener>) method.getDeclaringClass();
    }

    @Override
    public EventKey key() {
        return key;
    }

    @Override
    public boolean ignoreCancelled() {
        return ignoreCancelled;
    }

    @Override
    public Optional<AnnotatedElement> element() {
        return Optional.of(method());
    }

    public Method method() {
        return method;
    }

    public MethodHandle methodHandle() {
        if(methodHandle == null) {
            try {
                methodHandle = MethodHandles.lookup().unreflect(method());
            } catch(IllegalAccessException e) {
                // Should never happen, since we called setAccessible
                Throwables.propagate(e);
            }
        }
        return methodHandle;
    }

    @Override
    public ThrowingConsumer<E, Throwable> bindTo(Listener listener) {
        return methodHandle().bindTo(listener)::invokeWithArguments;
    }
}
