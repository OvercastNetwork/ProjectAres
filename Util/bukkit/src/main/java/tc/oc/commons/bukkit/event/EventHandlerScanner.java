package tc.oc.commons.bukkit.event;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import com.google.common.collect.SetMultimap;
import com.google.common.reflect.TypeToken;
import tc.oc.commons.core.exception.InvalidMemberException;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.minecraft.api.event.Listener;

/**
 * Scans classes for event handler methods that are denoted with a given annotation type.
 */
public abstract class EventHandlerScanner<Event, HandlerAnnotation extends Annotation, Info extends EventSubscriber<? extends Event>> {

    protected final Class<Listener> listenerType;
    protected final Class<Event> eventType;
    protected final Class<HandlerAnnotation> annotationType;

    public EventHandlerScanner() {
        listenerType = (Class<Listener>) new TypeToken<Listener>(getClass()){}.getRawType();
        eventType = (Class<Event>) new TypeToken<Event>(getClass()){}.getRawType();
        annotationType = (Class<HandlerAnnotation>) new TypeToken<HandlerAnnotation>(getClass()){}.getRawType();
    }

    protected abstract Info createHandlerInfo(Method method,
                                              Class<? extends Event> eventType,
                                              HandlerAnnotation annotation);

    public boolean isHandler(Method method) {
        return findAnnotation(method) != null &&
               !method.isBridge() &&
               !method.isSynthetic();
    }

    public @Nullable HandlerAnnotation findAnnotation(Method method) {
        return method.getAnnotation(annotationType);
    }

    public Class<? extends Event> findEventType(Method method) {
        if(Modifier.isStatic(method.getModifiers())) {
            throw new InvalidMemberException(method, "Event handler method cannot be static");
        }

        final Class<?>[] params = method.getParameterTypes();
        final Class<?> type = params[0];
        if(params.length != 1 || !eventType.isAssignableFrom(type)) {
            throw new InvalidMemberException(method, "Event handler method must take a " + eventType.getName() + " as its first parameter");
        }
        return type.asSubclass(eventType);
    }

    public Stream<Method> findEventHandlerMethods(Class<? extends Listener> listener) {
        return Stream.concat(
            Stream.of(listener.getDeclaredMethods())
                  .filter(this::isHandler),
            Stream.concat(Stream.of(listener.getSuperclass()),
                          Stream.of(listener.getInterfaces()))
                  .filter(ancestor -> ancestor != null && Listener.class.isAssignableFrom(ancestor))
                  .flatMap(ancestor -> findEventHandlerMethods((Class<? extends Listener>) ancestor))
        );
    }

    public SetMultimap<EventKey<? extends Event>, Info> findEventHandlers(Class<? extends Listener> listener) {
        return findEventHandlerMethods(listener)
            .map(method -> createHandlerInfo(method, findEventType(method), findAnnotation(method)))
            .collect(Collectors.toImmutableSetMultimap(EventSubscriber::key));
    }
}
